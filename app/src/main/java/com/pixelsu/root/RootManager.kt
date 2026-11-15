package com.pixelsu.root

import android.content.Context
import com.pixelsu.model.RootStatus
import com.pixelsu.model.RootType
import com.pixelsu.model.HideMode
import com.pixelsu.model.SELinuxStatus
import com.pixelsu.utils.ShellExecutor
import com.pixelsu.utils.PixelSULogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class RootManager(
    private val context: Context,
    private val shellExecutor: ShellExecutor,
    private val logger: PixelSULogger
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tempRootJob: Job? = null
    private val tempRootActive = AtomicBoolean(false)
    private val tempRootEndTime = AtomicLong(0)
    private val rootStatusCallback: ((RootStatus) -> Unit)? = null
    private var statusListener: ((RootStatus) -> Unit)? = null

    fun setStatusListener(listener: ((RootStatus) -> Unit)?) {
        this.statusListener = listener
    }

    suspend fun checkRootStatus(): RootStatus = withContext(Dispatchers.IO) {
        val isRooted = isRootAvailable()
        val rootType = if (isRooted) determineRootType() else RootType.UNKNOWN
        val tempRootActive = tempRootActive.get()
        val tempRootEndTimeVal = tempRootEndTime.get()
        val tempRootRemaining = if (tempRootActive) {
            val remaining = tempRootEndTimeVal - System.currentTimeMillis()
            if (remaining <= 0) 0L else remaining
        } else 0L

        val isHideActive = isHideActive()
        val hideMode = getHideMode()
        val selinuxStatus = getSELinuxStatus()

        RootStatus(
            isRooted = isRooted,
            rootType = rootType,
            isTempRootActive = tempRootActive,
            tempRootEndTime = if (tempRootActive) tempRootEndTimeVal else 0,
            isHideActive = isHideActive,
            hideMode = hideMode,
            selinuxStatus = selinuxStatus
        )
    }

    private suspend fun isRootAvailable(): Boolean {
        return try {
            val result = shellExecutor.executeCommand("which su")
            result.exitCode == 0 && result.output.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun determineRootType(): RootType {
        return try {
            val result = shellExecutor.executeCommand("su -c 'id'")
            if (result.exitCode == 0 && result.output.contains("uid=0")) {
                RootType.MAGISK
            } else {
                RootType.UNKNOWN
            }
        } catch (e: Exception) {
            RootType.UNKNOWN
        }
    }

    private suspend fun isHideActive(): Boolean {
        return try {
            val result = shellExecutor.executeCommand("magisk --hide-status 2>/dev/null || echo false")
            result.exitCode == 0 && result.output.trim() == "true"
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun getHideMode(): HideMode {
        return try {
            val result = shellExecutor.executeCommand("magisk --hide-mode 2>/dev/null || echo DENY_LIST")
            when (result.output.trim().uppercase()) {
                "ALLOW_LIST" -> HideMode.ALLOW_LIST
                else -> HideMode.DENY_LIST
            }
        } catch (e: Exception) {
            HideMode.DENY_LIST
        }
    }

    private suspend fun getSELinuxStatus(): SELinuxStatus {
        return try {
            val result = shellExecutor.executeCommand("getenforce")
            when (result.output.trim().lowercase()) {
                "enforcing" -> SELinuxStatus.ENFORCING
                "permissive" -> SELinuxStatus.PERMISSIVE
                else -> SELinuxStatus.DISABLED
            }
        } catch (e: Exception) {
            SELinuxStatus.UNKNOWN
        }
    }

    suspend fun enableRoot(): Boolean {
        return try {
            val result = shellExecutor.executeCommand("su -c 'setenforce 0'")
            if (result.exitCode == 0) {
                notifyStatusChange()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.e("RootManager", "Failed to enable root", e.toString())
            false
        }
    }

    suspend fun disableRoot(): Boolean {
        return try {
            val result = shellExecutor.executeCommand("su -c 'setenforce 1'")
            if (result.exitCode == 0) {
                notifyStatusChange()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.e("RootManager", "Failed to disable root", e.toString())
            false
        }
    }

    suspend fun enableTempRoot(durationMinutes: Long): Boolean {
        if (tempRootActive.get()) return false

        return try {
            val result = shellExecutor.executeCommand("su -c 'setenforce 0'")
            if (result.exitCode == 0) {
                tempRootActive.set(true)
                tempRootEndTime.set(System.currentTimeMillis() + durationMinutes * 60 * 1000)
                notifyStatusChange()
                
                tempRootJob = scope.launch {
                    kotlinx.coroutines.delay(durationMinutes * 60 * 1000)
                    disableTempRoot()
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.e("RootManager", "Failed to enable temp root", e.toString())
            false
        }
    }

    suspend fun disableTempRoot(): Boolean {
        if (!tempRootActive.get()) return true

        tempRootActive.set(false)
        tempRootJob?.cancel()
        
        return try {
            val result = shellExecutor.executeCommand("su -c 'setenforce 1'")
            if (result.exitCode == 0) {
                notifyStatusChange()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.e("RootManager", "Failed to disable temp root", e.toString())
            false
        }
    }

    suspend fun enableHide(): Boolean {
        return try {
            val result = shellExecutor.executeCommand("magisk --hide")
            if (result.exitCode == 0) {
                notifyStatusChange()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.e("RootManager", "Failed to enable hide", e.toString())
            false
        }
    }

    suspend fun disableHide(): Boolean {
        return try {
            val result = shellExecutor.executeCommand("magisk --unhide")
            if (result.exitCode == 0) {
                notifyStatusChange()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.e("RootManager", "Failed to disable hide", e.toString())
            false
        }
    }

    suspend fun setHideMode(mode: HideMode): Boolean {
        return try {
            val cmd = when (mode) {
                HideMode.ALLOW_LIST -> "magisk --hide-mode allowlist"
                else -> "magisk --hide-mode denylist"
            }
            val result = shellExecutor.executeCommand(cmd)
            if (result.exitCode == 0) {
                notifyStatusChange()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logger.e("RootManager", "Failed to set hide mode", e.toString())
            false
        }
    }

    private fun notifyStatusChange() {
        scope.launch {
            val status = checkRootStatus()
            statusListener?.invoke(status)
        }
    }

    fun shutdown() {
        scope.cancel()
    }
}
