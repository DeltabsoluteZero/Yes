package com.pixelsu.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pixelsu.common.AppComponent
import com.pixelsu.model.AppSettings
import com.pixelsu.utils.Logger

class PackageChangeReceiver : BroadcastReceiver() {

    private lateinit var logger: Logger

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as AppComponent
        logger = app.getLogger()
        
        val action = intent.action
        val packageName = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)?.replace("package:", "")
        
        logger.i("PackageChangeReceiver", "Package event: $action for $packageName")

        when (action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                if (packageName != null && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    handlePackageAdded(context, packageName)
                }
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                if (packageName != null && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    handlePackageRemoved(context, packageName)
                }
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                if (packageName != null) {
                    handlePackageReplaced(context, packageName)
                }
            }
        }
    }

    private fun handlePackageAdded(context: Context, packageName: String) {
        logger.i("PackageChangeReceiver", "New package installed: $packageName")
        
        // Notify hide service
        val hideIntent = Intent(context, RootHideService::class.java).apply {
            action = RootHideService.ACTION_PACKAGE_ADDED
            putExtra(RootHideService.EXTRA_PACKAGE_NAME, packageName)
        }
        context.startService(hideIntent)
    }

    private fun handlePackageRemoved(context: Context, packageName: String) {
        logger.i("PackageChangeReceiver", "Package removed: $packageName")
        
        // Notify hide service
        val hideIntent = Intent(context, RootHideService::class.java).apply {
            action = RootHideService.ACTION_PACKAGE_REMOVED
            putExtra(RootHideService.EXTRA_PACKAGE_NAME, packageName)
        }
        context.startService(hideIntent)
    }

    private fun handlePackageReplaced(context: Context, packageName: String) {
        logger.i("PackageChangeReceiver", "Package updated: $packageName")
        // Could refresh app info in hide manager
    }
}