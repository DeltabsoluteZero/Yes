package com.pixelsu.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pixelsu.R
import com.pixelsu.common.AppComponent
import com.pixelsu.model.RootStatus
import com.pixelsu.root.RootManager
import com.pixelsu.ui.main.MainActivity
import com.pixelsu.utils.Logger
import com.pixelsu.utils.ShellExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RootManagementService : Service() {

    companion object {
        const val ACTION_START = "\${applicationId}.ACTION_START_ROOT_SERVICE"
        const val ACTION_STOP = "\${applicationId}.ACTION_STOP_ROOT_SERVICE"
        const val ACTION_REFRESH = "\${applicationId}.ACTION_REFRESH_ROOT_STATUS"
        const val ACTION_TEMP_ROOT_EXPIRED = "\${applicationId}.ACTION_TEMP_ROOT_EXPIRED"

        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "root_management"
    }

    private lateinit var rootManager: RootManager
    private lateinit var shellExecutor: ShellExecutor
    private lateinit var logger: Logger
    private lateinit var notificationManager: NotificationManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var statusCheckJob: Job? = null
    private var tempRootMonitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
        createNotificationChannel()
    }

    private fun initializeComponents() {
        val app = applicationContext as AppComponent
        rootManager = app.getRootManager()
        shellExecutor = app.getShellExecutor()
        logger = app.getLogger()
    }

    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_root),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Root management status notifications"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_STICKY

        when (action) {
            ACTION_START -> startRootMonitoring()
            ACTION_STOP -> stopRootMonitoring()
            ACTION_REFRESH -> refreshRootStatus()
            ACTION_TEMP_ROOT_EXPIRED -> handleTempRootExpired()
        }

        return START_STICKY
    }

    private fun startRootMonitoring() {
        logger.i("RootManagementService", "Starting root monitoring")

        // Start periodic status checks
        statusCheckJob = scope.launch {
            while (true) {
                try {
                    val status = rootManager.checkRootStatus()
                    updateNotification(status)

                    // Check temp root expiry
                    if (status.isTempRootActive && status.isTempRootExpired) {
                        sendBroadcast(Intent(ACTION_TEMP_ROOT_EXPIRED))
                    }
                } catch (e: Exception) {
                    logger.e("RootManagementService", "Status check failed", e.toString())
                }

                kotlinx.coroutines.delay(30000) // Check every 30 seconds
            }
        }

        // Monitor temp root more frequently
        tempRootMonitorJob = scope.launch {
            while (true) {
                try {
                    val status = rootManager.checkRootStatus()
                    if (status.isTempRootActive) {
                        val remaining = status.tempRootRemaining
                        if (remaining > 0 && remaining <= 60000) { // Less than 1 minute
                            updateTempRootNotification(status, remaining)
                        }
                    }
                } catch (e: Exception) {
                    logger.e("RootManagementService", "Temp root monitor failed", e.toString())
                }
                kotlinx.coroutines.delay(10000) // Check every 10 seconds
            }
        }

        // Show initial notification
        scope.launch {
            val status = rootManager.checkRootStatus()
            updateNotification(status)
        }
    }

    private fun stopRootMonitoring() {
        logger.i("RootManagementService", "Stopping root monitoring")
        statusCheckJob?.cancel()
        tempRootMonitorJob?.cancel()
        stopForeground(true)
    }

    private fun refreshRootStatus() {
        scope.launch {
            val status = rootManager.checkRootStatus()
            updateNotification(status)
        }
    }

    private fun handleTempRootExpired() {
        logger.i("RootManagementService", "Temporary root expired")
        scope.launch {
            rootManager.disableTempRoot()
            val status = rootManager.checkRootStatus()
            updateNotification(status)
        }
    }

    private fun updateNotification(status: RootStatus) {
        val notification = buildNotification(status)
        val foregroundType = getFgServiceType()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, foregroundType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateTempRootNotification(status: RootStatus, remaining: Long) {
        val notification = buildTempRootNotification(status, remaining)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(status: RootStatus): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Extract notification details
        val details = getNotificationDetails(status)
        val title = details[0] as CharSequence
        val text = details[1] as CharSequence
        val icon = details[2] as Int
        val color = details[3] as Int

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon as Int)
            .setColor(color)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOngoing(status.isRooted || status.isTempRootActive)
            .setPriority(if (status.isTempRootActive) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setShowWhen(false)
            .build()
    }

    private fun getNotificationDetails(status: RootStatus): List<Any> {
        return when {
            status.isTempRootActive && !status.isTempRootExpired -> {
                val remaining = formatDuration(status.tempRootRemaining)
                listOf(
                    getString(R.string.notif_title_temp_root),
                    getString(R.string.notif_text_temp_root, formatDuration(status.tempRootRemaining)),
                    R.drawable.ic_temp_root,
                    getColor(R.color.warning)
                )
            }
            status.isRooted && status.isHideActive -> {
                listOf(
                    getString(R.string.notif_title_hide_active),
                    getString(R.string.notif_text_hide_active),
                    R.drawable.ic_root_hide,
                    getColor(R.color.info)
                )
            }
            status.isRooted -> {
                listOf(
                    getString(R.string.notif_title_root_active),
                    getString(R.string.notif_text_root_active),
                    R.drawable.ic_root_active,
                    getColor(R.color.success)
                )
            }
            else -> {
                listOf(
                    "Root Inactive",
                    "Tap to enable root access",
                    R.drawable.ic_root_inactive,
                    getColor(R.color.error)
                )
            }
        }
    }

    private fun buildTempRootNotification(status: RootStatus, remaining: Long): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_temp_root)
            .setColor(getColor(R.color.warning))
            .setContentTitle(getString(R.string.notif_title_temp_root))
            .setContentText(getString(R.string.notif_text_temp_root, formatDuration(remaining)))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setShowWhen(false)
            .setProgress(100, (remaining / 60000).toInt(), false)
            .build()
    }

    private fun formatDuration(millis: Long): String {
        val minutes = millis / 60000
        val seconds = (millis % 60000) / 1000
        return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
    }

    private fun getFgServiceType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+
            4 // FOREGROUND_SERVICE_TYPE_DATA_SYNC constant value
        } else {
            0
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopRootMonitoring()
        scope.cancel()
    }
}