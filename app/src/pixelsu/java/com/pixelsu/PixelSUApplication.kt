package com.pixelsu

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.pixelsu.common.AppComponent
import com.pixelsu.exploit.ExploitManager
import com.pixelsu.hide.HideManager
import com.pixelsu.model.AppSettings
import com.pixelsu.root.RootManager
import com.pixelsu.utils.PixelSULogger
import com.pixelsu.utils.ShellExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

class PixelSUApplication : Application(), AppComponent {

    private val appJob = SupervisorJob()
    private val appScope = CoroutineScope(appJob + Dispatchers.Main)
    private var settings: AppSettings = AppSettings.getDefaults()
    private var logger: PixelSULogger? = null
    private var shellExecutor: ShellExecutor? = null
    private var rootManager: RootManager? = null
    private var exploitManager: ExploitManager? = null
    private var hideManager: HideManager? = null

    override fun onCreate() {
        super.onCreate()

        // Use a simple check for debug mode since BuildConfig may not be available
        try {
            if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                Timber.plant(Timber.DebugTree())
            } else {
                Timber.plant(CrashReportingTree())
            }
        } catch (e: Exception) {
            Timber.plant(Timber.DebugTree())
        }

        loadSettings()

        try {
            logger = PixelSULogger(this, settings)
            logger?.i("PixelSUApplication", "Application created")

            shellExecutor = ShellExecutor(settings, logger!!)

            rootManager = RootManager(this, shellExecutor!!, logger!!)
            exploitManager = ExploitManager(this, shellExecutor!!, logger!!)
            hideManager = HideManager(this, shellExecutor!!, logger!!)

            ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize PixelSUApplication components")
            // Initialize minimal logger for error reporting
            logger = PixelSULogger(this, settings)
        }
    }

    private fun loadSettings() {
        settings = AppSettings.getDefaults()
    }

    override fun getSettings(): AppSettings = settings

    override fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
        logger?.i("PixelSUApplication", "Settings updated")
    }

    override fun getLogger(): PixelSULogger = logger!!

    fun getLoggerInterface(): com.pixelsu.utils.Logger = logger!!

    override fun getShellExecutor(): ShellExecutor = shellExecutor!!
    override fun getRootManager(): RootManager = rootManager!!
    override fun getExploitManager(): ExploitManager = exploitManager!!
    override fun getHideManager(): HideManager = hideManager!!
    override fun getAppScope(): CoroutineScope = appScope

    override fun onTerminate() {
        super.onTerminate()
        logger?.shutdown()
        rootManager?.shutdown()
        exploitManager?.shutdown()
        hideManager?.shutdown()
        appJob.cancel()
    }

    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.ERROR) {
            }
        }
    }

    private class AppLifecycleObserver : androidx.lifecycle.DefaultLifecycleObserver {
        override fun onStart(owner: androidx.lifecycle.LifecycleOwner) {
            Timber.d("AppLifecycle", "App started")
        }

        override fun onStop(owner: androidx.lifecycle.LifecycleOwner) {
            Timber.d("AppLifecycle", "App stopped")
        }
    }
}