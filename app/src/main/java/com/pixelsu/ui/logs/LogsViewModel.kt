package com.pixelsu.ui.logs

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pixelsu.model.LogEntry
import com.pixelsu.utils.PixelSULogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class LogsViewModel(
    private val logger: PixelSULogger,
    private val context: Context
) : ViewModel() {

    private val _logs = MutableLiveData<List<LogEntry>>()
    val logs: LiveData<List<LogEntry>> = _logs

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadLogs() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logs = logger.getMemoryLogs()
                _logs.postValue(logs)
            } catch (e: Exception) {
                _error.postValue("Failed to load logs: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                logger.clearMemoryLogs()
                logger.clearLogFile()
                _logs.postValue(emptyList())
            } catch (e: Exception) {
                _error.postValue("Failed to clear logs: ${e.message}")
            }
        }
    }

    fun exportLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.getExternalFilesDir(null), "dfroot_logs_${System.currentTimeMillis()}.txt")
                val success = logger.exportLogs(file)
                if (success) {
                    _error.postValue("Logs exported to ${file.absolutePath}")
                } else {
                    _error.postValue("Failed to export logs")
                }
            } catch (e: Exception) {
                _error.postValue("Export failed: ${e.message}")
            }
        }
    }

    class Factory(
        private val logger: PixelSULogger,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LogsViewModel(logger, context) as T
        }
    }
}
