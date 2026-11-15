package com.pixelsu.hide

import android.content.Context
import android.content.pm.PackageManager
import com.pixelsu.model.AppInfo
import com.pixelsu.model.HideListConfig
import com.pixelsu.model.HideListMode
import com.pixelsu.model.HideRule
import com.pixelsu.utils.ShellExecutor
import com.pixelsu.utils.PixelSULogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class HideManager(
    private val context: Context,
    private val shellExecutor: ShellExecutor,
    private val logger: PixelSULogger
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var hideConfig = HideListConfig()
    private var installedApps: List<AppInfo> = emptyList()

    interface HideListener {
        fun onHideStatusChanged(hideActive: Boolean, mode: String)
        fun onAppListChanged(apps: List<AppInfo>)
        fun onConfigChanged(config: HideListConfig)
    }

    private var listener: HideListener? = null

    fun setListener(listener: HideListener?) {
        this.listener = listener
    }

    fun getHideConfig(): HideListConfig = hideConfig

    fun getInstalledApps(): List<AppInfo> = installedApps

    suspend fun loadApps() = withContext(Dispatchers.IO) {
        logger.i("HideManager", "Loading installed apps")
        installedApps = getInstalledApplications()
        listener?.onAppListChanged(installedApps)
    }

    private fun getInstalledApplications(): List<AppInfo> {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val apps = mutableListOf<AppInfo>()

        for (pkg in packages) {
            try {
                val appInfo = pkg.applicationInfo?.let { appInfo ->
                    AppInfo.fromPackageInfo(pkg, packageManager.getApplicationLabel(appInfo).toString())
                }
                appInfo?.let { apps.add(it) }
            } catch (e: Exception) {
                // Skip apps that can't be loaded
            }
        }

        return apps.sortedBy { it.appName.lowercase() }
    }

    suspend fun updateHideConfig(config: HideListConfig) = withContext(Dispatchers.IO) {
        logger.i("HideManager", "Updating hide config: mode=${config.mode}")
        hideConfig = config.copy(lastModified = System.currentTimeMillis())
        applyHideConfig()
        listener?.onConfigChanged(hideConfig)
    }

    suspend fun applyHideConfig(): Boolean = withContext(Dispatchers.IO) {
        logger.i("HideManager", "Applying hide configuration")
        
        var success = true
        
        // Apply Magisk Hide / Zygisk deny list
        if (hideConfig.hideFromMagiskHide || hideConfig.hideFromZygisk) {
            success = success && configureMagiskHide()
        }

        // Apply Shamiko
        if (hideConfig.hideFromShamiko) {
            success = success && configureShamiko()
        }

        // Apply Kernel Hide (KernelSU)
        if (hideConfig.hideFromKernel) {
            success = success && configureKernelHide()
        }

        // Apply custom rules
        for (rule in hideConfig.customRules) {
            if (rule.enabled) {
                success = success && applyCustomRule(rule)
            }
        }

        listener?.onHideStatusChanged(success, hideConfig.mode.name)
        success
    }

    private suspend fun configureMagiskHide(): Boolean = withContext(Dispatchers.IO) {
        val packages = getTargetPackages()
        
        if (hideConfig.mode == HideListMode.DENY_LIST) {
            // Add packages to deny list
            for (pkg in packages) {
                val result = shellExecutor.executeRootCommand("magisk --denylist add $pkg", 5000)
                if (result.exitCode != 0) {
                    logger.w("HideManager", "Failed to add $pkg to Magisk deny list: ${result.error}")
                    return@withContext false
                }
            }
        } else {
            // Allow list mode - clear deny list and add only allowed packages
            val clearResult = shellExecutor.executeRootCommand("magisk --denylist clear", 5000)
            if (clearResult.exitCode != 0) return@withContext false
            
            // In allow list mode, we add all apps EXCEPT the allowed ones to deny list
            // This is a simplified approach; real implementation would be more complex
            for (pkg in packages) {
                val result = shellExecutor.executeRootCommand("magisk --denylist add $pkg", 5000)
                if (result.exitCode != 0) return@withContext false
            }
        }
        
        // Enable deny list
        val enableResult = shellExecutor.executeRootCommand("magisk --denylist enable", 5000)
        enableResult.exitCode == 0
    }

    private suspend fun configureZygisk(): Boolean = withContext(Dispatchers.IO) {
        // Enable Zygisk
        val result = shellExecutor.executeRootCommand("magisk --zygisk enable", 5000)
        if (result.exitCode != 0) return@withContext false
        
        // Configure deny list via Zygisk
        configureMagiskHide()
    }

    private suspend fun configureShamiko(): Boolean = withContext(Dispatchers.IO) {
        // Shamiko is a separate module, check if installed
        val checkResult = shellExecutor.executeCommand("ls /data/adb/modules/shamiko", 5000)
        if (checkResult.exitCode != 0) {
            logger.w("HideManager", "Shamiko module not found")
            return@withContext false
        }
        
        // Shamiko configuration is typically done via its own app or config file
        // We can write to its config file if accessible
        val configFile = "/data/adb/modules/shamiko/config"
        val packages = getTargetPackages().joinToString(",")
        val configContent = "hide_list=$packages\nmode=${hideConfig.mode.name.lowercase()}\n"
        
        val writeResult = shellExecutor.executeRootCommand("echo '$configContent' > $configFile", 5000)
        writeResult.exitCode == 0
    }

    private suspend fun configureKernelHide(): Boolean = withContext(Dispatchers.IO) {
        // KernelSU hide configuration
        val packages = getTargetPackages()
        
        for (pkg in packages) {
            val result = shellExecutor.executeRootCommand("ksu --hide add $pkg", 5000)
            if (result.exitCode != 0) {
                logger.w("HideManager", "Failed to add $pkg to KernelSU hide list: ${result.error}")
                return@withContext false
            }
        }
        
        val enableResult = shellExecutor.executeRootCommand("ksu --hide enable", 5000)
        enableResult.exitCode == 0
    }

    private suspend fun applyCustomRule(rule: HideRule): Boolean = withContext(Dispatchers.IO) {
        // Apply custom hide rule for specific package
        // This could involve modifying props, mounting overlays, etc.
        
        var success = true
        
        if (rule.hideRoot) {
            // Hide su binary from this app
            val result = shellExecutor.executeRootCommand("magisk --denylist add ${rule.packageName}", 5000)
            success = success && result.exitCode == 0
        }
        
        if (rule.hideMagiskFiles) {
            // Hide Magisk files from this app
            // This would typically use mount namespaces or overlayfs
        }
        
        if (rule.hideProps) {
            // Hide custom props
            for ((key, value) in rule.customProps) {
                val result = shellExecutor.executeRootCommand("resetprop -n $key $value", 5000)
                success = success && result.exitCode == 0
            }
        }
        
        success
    }

    private fun getTargetPackages(): List<String> {
        return if (hideConfig.mode == HideListMode.DENY_LIST) {
            hideConfig.targetPackages
        } else {
            // Allow list: all installed apps except those in targetPackages
            installedApps
                .filter { (hideConfig.includeSystemApps || !it.isSystemApp) }
                .map { it.packageName }
                .filterNot { it in hideConfig.targetPackages }
        }
    }

    suspend fun addPackageToHideList(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (packageName in hideConfig.targetPackages) return@withContext true
        
        hideConfig = hideConfig.copy(
            targetPackages = hideConfig.targetPackages + packageName,
            lastModified = System.currentTimeMillis()
        )
        
        applyHideConfig()
        listener?.onConfigChanged(hideConfig)
        true
    }

    suspend fun removePackageFromHideList(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (packageName !in hideConfig.targetPackages) return@withContext true
        
        hideConfig = hideConfig.copy(
            targetPackages = hideConfig.targetPackages.filterNot { it == packageName },
            lastModified = System.currentTimeMillis()
        )
        
        applyHideConfig()
        listener?.onConfigChanged(hideConfig)
        true
    }

    suspend fun togglePackageHide(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (packageName in hideConfig.targetPackages) {
            removePackageFromHideList(packageName)
        } else {
            addPackageToHideList(packageName)
        }
    }

    suspend fun setHideMode(mode: HideListMode): Boolean = withContext(Dispatchers.IO) {
        hideConfig = hideConfig.copy(
            mode = mode,
            lastModified = System.currentTimeMillis()
        )
        applyHideConfig()
        listener?.onConfigChanged(hideConfig)
        true
    }

    suspend fun enableHide(): Boolean = withContext(Dispatchers.IO) {
        applyHideConfig()
    }

    suspend fun disableHide(): Boolean = withContext(Dispatchers.IO) {
        logger.i("HideManager", "Disabling all root hiding")
        
        var success = true
        
        // Disable Magisk deny list
        val result1 = shellExecutor.executeRootCommand("magisk --denylist disable", 5000)
        success = success && result1.exitCode == 0
        
        // Disable Zygisk
        val result2 = shellExecutor.executeRootCommand("magisk --zygisk disable", 5000)
        success = success && result2.exitCode == 0
        
        // Disable KernelSU hide
        val result3 = shellExecutor.executeRootCommand("ksu --hide disable", 5000)
        success = success && result3.exitCode == 0
        
        // Clear deny list
        val result4 = shellExecutor.executeRootCommand("magisk --denylist clear", 5000)
        success = success && result4.exitCode == 0
        
        listener?.onHideStatusChanged(false, "NONE")
        success
    }

    suspend fun checkHideStatus(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        // Check Magisk deny list
        val magiskResult = shellExecutor.executeRootCommand("magisk --denylist status", 5000)
        if (magiskResult.exitCode == 0 && magiskResult.output.contains("enabled")) {
            return@withContext Pair(true, "Magisk DenyList/Zygisk")
        }
        
        // Check Zygisk
        val zygiskResult = shellExecutor.executeRootCommand("magisk --zygisk status", 5000)
        if (zygiskResult.exitCode == 0 && zygiskResult.output.contains("enabled")) {
            return@withContext Pair(true, "Zygisk")
        }
        
        // Check KernelSU hide
        val kernelResult = shellExecutor.executeRootCommand("ksu --hide status", 5000)
        if (kernelResult.exitCode == 0 && kernelResult.output.contains("enabled")) {
            return@withContext Pair(true, "KernelSU Hide")
        }
        
        // Check Shamiko
        val shamikoResult = shellExecutor.executeCommand("ls /data/adb/modules/shamiko", 5000)
        if (shamikoResult.exitCode == 0) {
            return@withContext Pair(true, "Shamiko")
        }
        
        Pair(false, "None")
    }

    fun searchApps(query: String): List<AppInfo> {
        val lowerQuery = query.lowercase()
        return installedApps.filter { app ->
            app.appName.lowercase().contains(lowerQuery) ||
            app.packageName.lowercase().contains(lowerQuery)
        }
    }

    fun getAppsByCategory(): Map<String, List<AppInfo>> {
        return installedApps.groupBy { app ->
            if (app.isSystemApp) "System Apps" else "User Apps"
        }
    }

    fun shutdown() {
        scope.cancel()
    }
}
