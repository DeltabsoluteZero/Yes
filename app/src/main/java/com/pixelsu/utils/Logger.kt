package com.pixelsu.utils

import androidx.lifecycle.LiveData
import com.pixelsu.model.LogEntry
import com.pixelsu.model.LogLevel
import com.pixelsu.model.LogCategory
import java.io.File

/**
 * Common Logger interface implemented by flavor-specific loggers
 */
interface Logger {

    val logs: LiveData<LogEntry>

    fun log(entry: LogEntry)

    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: String? = null,
        category: LogCategory = LogCategory.GENERAL,
        relatedExploitId: String? = null,
        relatedPackage: String? = null
    )

    fun v(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL)
    fun d(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL)
    fun i(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL)
    fun w(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL)
    fun e(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL)
    fun wtf(tag: String, message: String, throwable: String? = null, category: LogCategory = LogCategory.GENERAL)

    fun getMemoryLogs(): List<LogEntry>
    fun clearMemoryLogs()
    fun clearLogFile()
    suspend fun exportLogs(destination: File): Boolean
    fun setLoggingEnabled(enabled: Boolean)
    fun shutdown()
}