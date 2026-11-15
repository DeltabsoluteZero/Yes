package com.pixelsu.utils

import android.os.Build
import android.util.Log
import com.pixelsu.model.AppSettings
import com.pixelsu.model.LogEntry
import com.pixelsu.model.LogLevel
import com.pixelsu.model.LogCategory
import com.pixelsu.model.ShellType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit

class ShellExecutor(
    private val settings: AppSettings,
    private val logger: PixelSULogger
) {

    companion object {
        private const val TAG = "ShellExecutor"
        private const val DEFAULT_TIMEOUT_MS = 30000L
        private const val ROOT_TIMEOUT_MS = 60000L
    }

    data class CommandResult(
        val exitCode: Int,
        val output: String,
        val error: String,
        val duration: Long
    )

    suspend fun executeCommand(
        command: String,
        timeout: Long = DEFAULT_TIMEOUT_MS,
        useRoot: Boolean = false,
        workingDir: String? = null,
        environment: Map<String, String> = emptyMap()
    ): CommandResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var process: Process? = null
        var output = StringBuilder()
        var error = StringBuilder()
        var exitCode = -1

        try {
            val shell = getShellExecutable()
            val commandList = buildCommandList(shell, command, useRoot)

            val processBuilder = ProcessBuilder(commandList)
            processBuilder.redirectErrorStream(false)
            
            if (workingDir != null) {
                processBuilder.directory(File(workingDir))
            }
            
            environment.forEach { (key, value) ->
                processBuilder.environment()[key] = value
            }

            process = processBuilder.start()

            val outputThread = Thread {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    reader.forEachLine { line ->
                        output.append(line).append("\n")
                    }
                }
            }.also { it.start() }

            val errorThread = Thread {
                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    reader.forEachLine { line ->
                        error.append(line).append("\n")
                    }
                }
            }.also { it.start() }

            if (useRoot) {
                OutputStreamWriter(process.outputStream).use { writer ->
                    writer.write("$command\n")
                    writer.write("exit\n")
                    writer.flush()
                }
            }

            val finished = process.waitFor(timeout, TimeUnit.MILLISECONDS)
            
            outputThread.join(1000)
            errorThread.join(1000)

            if (!finished) {
                process.destroyForcibly()
                logger.log(LogEntry(
                    level = LogLevel.WARN,
                    tag = TAG,
                    message = "Command timed out after ${timeout}ms: $command",
                    category = LogCategory.GENERAL
                ))
                return@withContext CommandResult(-1, output.toString(), "Timeout", System.currentTimeMillis() - startTime)
            }

            exitCode = process.exitValue()

            logger.log(LogEntry(
                level = LogLevel.DEBUG,
                tag = TAG,
                message = "Command executed (exit=$exitCode): $command",
                category = LogCategory.GENERAL
            ))

        } catch (e: Exception) {
            logger.log(LogEntry(
                level = LogLevel.ERROR,
                tag = TAG,
                message = "Command execution failed: $command",
                throwable = e.toString(),
                category = LogCategory.ERROR
            ))
            return@withContext CommandResult(-1, output.toString(), e.message ?: e.toString(), System.currentTimeMillis() - startTime)
        } finally {
            process?.destroy()
        }

        CommandResult(exitCode, output.toString().trim(), error.toString().trim(), System.currentTimeMillis() - startTime)
    }

    suspend fun executeRootCommand(
        command: String,
        timeout: Long = ROOT_TIMEOUT_MS
    ): CommandResult = executeCommand(command, timeout, useRoot = true)

    suspend fun executeMultipleCommands(
        commands: List<String>,
        useRoot: Boolean = false,
        stopOnError: Boolean = true
    ): List<CommandResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<CommandResult>()
        for (command in commands) {
            val result = executeCommand(command, useRoot = useRoot)
            results.add(result)
            if (stopOnError && result.exitCode != 0) {
                break
            }
        }
        results
    }

    private fun getShellExecutable(): String {
        val shellType: ShellType = settings.preferredShell
        return when (shellType) {
            ShellType.SH -> "/system/bin/sh"
            ShellType.BASH -> findBinary("bash") ?: "/system/bin/sh"
            ShellType.ZSH -> findBinary("zsh") ?: "/system/bin/sh"
            ShellType.TOYBOX -> findBinary("toybox") ?: "/system/bin/sh"
            ShellType.TOOLBOX -> findBinary("toolbox") ?: "/system/bin/sh"
            else -> findBestShell()
        }
    }

    private fun findBestShell(): String {
        return findBinary("bash") ?: findBinary("toybox") ?: findBinary("toolbox") ?: "/system/bin/sh"
    }

    private fun findBinary(name: String): String? {
        val paths = listOf(
            "/system/bin/$name",
            "/system/xbin/$name",
            "/vendor/bin/$name",
            "/data/adb/magisk/$name",
            "/data/local/tmp/$name"
        )
        return paths.firstOrNull { File(it).exists() && File(it).canExecute() }
    }

    private fun buildCommandList(shell: String, command: String, useRoot: Boolean): List<String> {
        return if (useRoot) {
            val suPaths = listOf(
                "/system/bin/su",
                "/system/xbin/su",
                "/system/sbin/su",
                "/sbin/su",
                "/vendor/bin/su",
                "/data/adb/magisk/su",
                settings.magiskBinaryPath
            )
            val suBinary = suPaths.firstOrNull { File(it).exists() && File(it).canExecute() } ?: "su"
            listOf(suBinary, "-c", command)
        } else {
            listOf(shell, "-c", command)
        }
    }

    suspend fun checkRootAccess(): Boolean {
        val result = executeRootCommand("id", 5000)
        return result.exitCode == 0 && result.output.contains("uid=0")
    }

    suspend fun getRootShell(): String? {
        val result = executeRootCommand("which su", 5000)
        return if (result.exitCode == 0) result.output.trim() else null
    }

    suspend fun getSELinuxStatus(): String {
        val result = executeCommand("getenforce", 5000)
        return if (result.exitCode == 0) result.output.trim() else "unknown"
    }

    suspend fun setSELinuxMode(mode: String): Boolean {
        val result = executeRootCommand("setenforce ${mode.lowercase()}", 5000)
        return result.exitCode == 0
    }

    suspend fun getMagiskVersion(): Pair<String, Int> {
        val result = executeCommand("${settings.magiskBinaryPath} --version", 5000)
        if (result.exitCode == 0) {
            val version = result.output.trim()
            val versionCode = try {
                version.split(".").take(3).joinToString("").toInt()
            } catch (e: Exception) {
                0
            }
            return Pair(version, versionCode)
        }
        return Pair("", 0)
    }

    suspend fun isMagiskInstalled(): Boolean {
        val paths = listOf(
            "/data/adb/magisk/magisk",
            "/sbin/magisk",
            "/system/bin/magisk",
            "/system/xbin/magisk"
        )
        return paths.any { File(it).exists() && File(it).canExecute() }
    }

    suspend fun getKernelVersion(): String {
        val result = executeCommand("cat /proc/version", 5000)
        return if (result.exitCode == 0) result.output.trim() else "unknown"
    }

    suspend fun getDeviceInfo(): DeviceInfo {
        val model = executeCommand("getprop ro.product.model", 1000).output.trim()
        val androidVersion = executeCommand("getprop ro.build.version.release", 1000).output.trim()
        val apiLevel = executeCommand("getprop ro.build.version.sdk", 1000).output.trim().toIntOrNull() ?: 0
        val arch = System.getProperty("os.arch")
        val kernel = getKernelVersion()
        val selinux = getSELinuxStatus()
        val uptime = executeCommand("cat /proc/uptime", 1000).output.split(" ")[0].toDoubleOrNull()?.toLong() ?: 0L

        return DeviceInfo(model, androidVersion, apiLevel, arch, kernel, selinux, uptime)
    }

    data class DeviceInfo(
        val model: String,
        val androidVersion: String,
        val apiLevel: Int,
        val architecture: String,
        val kernelVersion: String,
        val selinuxStatus: String,
        val uptime: Long
    )
}
