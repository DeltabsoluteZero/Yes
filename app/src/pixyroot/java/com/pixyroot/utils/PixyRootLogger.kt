package com.pixyroot.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pixyroot.model.AppSettings
import com.pixyroot.model.LogEntry
import com.pixyroot.model.LogLevel
import com.pixyroot.model.LogCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class PixyRootLogger(
    private val context: Context,
    private val settings: AppSettings
) {

    companion object {
        private const val TAG = "PixyRootLogger"
        private const val LOG_FILE_NAME = "pixyroot.log"
        private const val MAX_LOG_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private const val MAX_MEMORY_LOGS = 1000
    }

    private val memoryLogs = ConcurrentLinkedQueue<LogEntry>()
    private val logFile = File(context.filesDir, LOG_FILE_NAME)
    private val fileWriter: FileWriter? by lazy { 
        try {
            FileWriter(logFile, true)
        } catch (e: Exception) {
            null
        }
    }
    private val printWriter: PrintWriter? = fileWriter?.let { PrintWriter(it, true) }
    private val isLoggingEnabled = AtomicBoolean(true)
    private val logLiveData = MutableLiveData<LogEntry>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    val logs: LiveData<LogEntry> = logLiveData

    fun log(entry: LogEntry) {
        if (!isLoggingEnabled.get()) return
        if (entry.level.ordinal < settings.logLevel.ordinal) return

        memoryLogs.add(entry)
        while (memoryLogs.size > MAX_MEMORY_LOGS) {
            memoryLogs.poll()
        }

        writeToFile(entry)

        logLiveData.postValue(entry)

        logToLogcat(entry)
    }

    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: String? = null,
        category: LogCategory = LogCategory.GENERAL,
        relatedExploitId: String? = null,
        relatedPackage: String? = null
    ) {
        log(LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            throwable = throwable,
            threadName = Thread.currentThread().name,
            category = category,
            relatedExploitId = relatedExploitId,
            relatedPackage = relatedPackage
        ))
    }

    fun v(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL) {
        log(LogLevel.VERBOSE, tag, message, throwable, category)
    }

    fun d(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL) {
        log(LogLevel.DEBUG, tag, message, throwable, category)
    }

    fun i(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL) {
        log(LogLevel.INFO, tag, message, throwable, category)
    }

    fun w(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL) {
        log(LogLevel.WARN, tag, message, throwable, category)
    }

    fun e(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL) {
        log(LogLevel.ERROR, tag, message, throwable, category)
    }

    fun wtf(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL) {
        log(LogLevel.ASSERT, tag, message, throwable, category)
    }

    private fun writeToFile(entry: LogEntry) {
        printWriter?.println(formatLogEntry(entry))
        
        if (logFile.length() > MAX_LOG_FILE_SIZE) {
            rotateLogFile()
        }
    }

    private fun formatLogEntry(entry: LogEntry): String {
        val timestamp = dateFormat.format(Date(entry.timestamp))
        val thread = entry.threadName
        val level = entry.level.name.padEnd(5)
        val category = entry.category.name.padEnd(20)
        val tag = entry.tag
        val message = entry.message
        val throwable = entry.throwable?.let { "\n    $it" } ?: ""
        val exploit = entry.relatedExploitId?.let { " [Exploit: $it]" } ?: ""
        val pkg = entry.relatedPackage?.let { " [Pkg: $it]" } ?: ""
        return "[$timestamp] [$level] [$category] [$thread] $tag: $message$throwable$exploit$pkg"
    }

    private fun rotateLogFile() {
        printWriter?.close()
        val rotatedFile = File(context.filesDir, "pixyroot_${dateFormat.format(Date()).replace(" ", "_").replace(":", "-")}.log")
        logFile.renameTo(rotatedFile)
        try {
            FileWriter(logFile, true).close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rotate log file", e)
        }
    }

    private fun logToLogcat(entry: LogEntry) {
        val priority = when (entry.level) {
            LogLevel.VERBOSE -> Log.VERBOSE
            LogLevel.DEBUG -> Log.DEBUG
            LogLevel.INFO -> Log.INFO
            LogLevel.WARN -> Log.WARN
            LogLevel.ERROR -> Log.ERROR
            LogLevel.ASSERT -> Log.ASSERT
        }
        val tag = "PixyRoot:${entry.tag}"
        val msg = entry.message + entry.throwable?.let { "\n$it" } ?: ""
        Log.println(priority, tag, msg)
    }

    fun getMemoryLogs(): List<LogEntry> = memoryLogs.toList()

    fun clearMemoryLogs() {
        memoryLogs.clear()
    }

    fun clearLogFile() {
        printWriter?.close()
        logFile.delete()
        try {
            FileWriter(logFile, true).close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to recreate log file", e)
        }
        clearMemoryLogs()
    }

    suspend fun exportLogs(destination: File): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            FileWriter(destination).use { writer ->
                writer.write("PixyRoot Log Export - ${dateFormat.format(Date())}\n")
                writer.write("Device: ${android.os.Build.MODEL} (API ${android.os.Build.VERSION.SDK_INT})\n")
                writer.write("App Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}\n\n")
                
                memoryLogs.forEach { entry ->
                    writer.write(formatLogEntry(entry))
                    writer.write("\n")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export logs", e)
            false
        }
    }

    fun setLoggingEnabled(enabled: Boolean) {
        isLoggingEnabled.set(enabled)
    }

    fun shutdown() {
        printWriter?.close()
        fileWriter?.close()
    }
}