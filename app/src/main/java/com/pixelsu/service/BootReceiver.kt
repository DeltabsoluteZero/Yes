package com.pixelsu.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pixelsu.common.AppComponent
import com.pixelsu.model.AppSettings
import com.pixelsu.model.HideListConfig
import com.pixelsu.utils.Logger

class BootReceiver : BroadcastReceiver() {

    private lateinit var logger: Logger

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as AppComponent
        logger = app.getLogger()
        
        val action = intent.action
        logger.i("BootReceiver", "Received broadcast: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "\${applicationId}.ACTION_RESTORE_ROOT" -> {
                handleBootCompleted(context)
            }
        }
    }

    private fun handleBootCompleted(context: Context) {
        logger.i("BootReceiver", "Boot completed, restoring root state")

        // Check if auto-start is enabled
        val settings = AppSettings.getDefaults()
        if (settings.autoStartOnBoot) {
            // Start root management service
            val serviceIntent = Intent(context, RootManagementService::class.java).apply {
                action = RootManagementService.ACTION_START
            }
            context.startForegroundService(serviceIntent)

            // Start hide service if configured
            val hideConfig = HideListConfig()
            if (hideConfig.targetPackages.isNotEmpty()) {
                val hideIntent = Intent(context, RootHideService::class.java).apply {
                    action = RootHideService.ACTION_APPLY_HIDE
                }
                context.startForegroundService(hideIntent)
            }

            logger.i("BootReceiver", "Root services started on boot")
        }
    }
}