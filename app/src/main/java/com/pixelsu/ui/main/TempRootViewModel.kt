package com.pixelsu.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pixelsu.model.RootStatus
import com.pixelsu.root.RootManager
import com.pixelsu.utils.PixelSULogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TempRootViewModel(
    private val rootManager: RootManager,
    private val logger: PixelSULogger
) : ViewModel() {

    private val _rootStatus = MutableLiveData<RootStatus>()
    val rootStatus: LiveData<RootStatus> = _rootStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        _isLoading.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val status = rootManager.checkRootStatus()
                _rootStatus.postValue(status)
            } catch (e: Exception) {
                logger.e("TempRootViewModel", "Failed to refresh status", e.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun enableTempRoot(durationMinutes: Long) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = rootManager.enableTempRoot(durationMinutes)
                if (success) {
                    val status = rootManager.checkRootStatus()
                    _rootStatus.postValue(status)
                } else {
                    _error.postValue("Failed to enable temporary root")
                }
            } catch (e: Exception) {
                logger.e("TempRootViewModel", "Enable temp root failed", e.toString())
                _error.postValue("Enable temp root failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun disableTempRoot() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = rootManager.disableTempRoot()
                if (success) {
                    val status = rootManager.checkRootStatus()
                    _rootStatus.postValue(status)
                } else {
                    _error.postValue("Failed to disable temporary root")
                }
            } catch (e: Exception) {
                logger.e("TempRootViewModel", "Disable temp root failed", e.toString())
                _error.postValue("Disable temp root failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    class Factory(
        private val rootManager: RootManager,
        private val logger: PixelSULogger
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TempRootViewModel(rootManager, logger) as T
        }
    }
}
