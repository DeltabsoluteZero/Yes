package com.pixelsu.common

import com.pixelsu.model.AppSettings
import com.pixelsu.root.RootManager
import com.pixelsu.exploit.ExploitManager
import com.pixelsu.hide.HideManager
import com.pixelsu.utils.ShellExecutor
import com.pixelsu.utils.Logger
import kotlinx.coroutines.CoroutineScope

/**
 * Common interface for flavor-specific Application components
 * This allows main source set code to access app components without knowing the flavor
 */
interface AppComponent {

    fun getSettings(): AppSettings
    fun updateSettings(newSettings: AppSettings)
    fun getLogger(): Logger
    fun getShellExecutor(): ShellExecutor
    fun getRootManager(): RootManager
    fun getExploitManager(): ExploitManager
    fun getHideManager(): HideManager
    fun getAppScope(): CoroutineScope
}