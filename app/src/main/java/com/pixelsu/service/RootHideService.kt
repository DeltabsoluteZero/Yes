package com.pixelsu.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.pixelsu.common.AppComponent
import com.pixelsu.hide.HideManager
import com.pixelsu.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RootHideService : Service() {

    companion object {
        const val ACTION_APPLY_HIDE = "\${applicationId}.ACTION_APPLY_HIDE"
        const val ACTION_REMOVE_HIDE = "\${applicationId}.ACTION_REMOVE_HIDE"
        const val ACTION_REFRESH_HIDE_STATUS = "\${applicationId}.ACTION_REFRESH_HIDE_STATUS"
        const val ACTION_PACKAGE_ADDED = "\${applicationId}.ACTION_PACKAGE_ADDED"
        const val ACTION_PACKAGE_REMOVED = "\${applicationId}.ACTION_PACKAGE_REMOVED"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }

    private lateinit var hideManager: HideManager
    private lateinit var logger: Logger
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var statusCheckJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val app = applicationContext as AppComponent
        hideManager = app.getHideManager()
        logger = app.getLogger()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_STICKY

        when (action) {
            ACTION_APPLY_HIDE -> scope.launch { hideManager.enableHide() }
            ACTION_REMOVE_HIDE -> scope.launch { hideManager.disableHide() }
            ACTION_REFRESH_HIDE_STATUS -> scope.launch {
                val (active, mode) = hideManager.checkHideStatus()
                // Broadcast status
            }
            ACTION_PACKAGE_ADDED -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                packageName?.let { scope.launch { handlePackageAdded(it) } }
            }
            ACTION_PACKAGE_REMOVED -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                packageName?.let { scope.launch { handlePackageRemoved(it) } }
            }
        }

        return START_STICKY
    }

    private suspend fun handlePackageAdded(packageName: String) {
        logger.i("RootHideService", "Package added: $packageName")
        // Check if this package should be hidden based on config
        val config = hideManager.getHideConfig()
        if (packageName in config.targetPackages) {
            hideManager.addPackageToHideList(packageName)
        }
    }

    private suspend fun handlePackageRemoved(packageName: String) {
        logger.i("RootHideService", "Package removed: $packageName")
        // Remove from hide list if present
        hideManager.removePackageFromHideList(packageName)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        statusCheckJob?.cancel()
        scope.cancel()
    }
}