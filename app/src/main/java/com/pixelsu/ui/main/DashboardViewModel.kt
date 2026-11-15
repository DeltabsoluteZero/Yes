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

class DashboardViewModel(
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
        rootManager.setStatusListener { status ->
            _rootStatus.postValue(status)
        }
        refreshRootStatus()
    }

    fun refreshRootStatus() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val status = rootManager.checkRootStatus()
                _rootStatus.postValue(status)
            } catch (e: Exception) {
                logger.e("DashboardViewModel", "Failed to refresh root status", e.toString())
                _error.postValue("Failed to check root status: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun enableRoot() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = rootManager.enableRoot()
                if (success) {
                    val status = rootManager.checkRootStatus()
                    _rootStatus.postValue(status)
                } else {
                    _error.postValue("Failed to enable root access")
                }
            } catch (e: Exception) {
                logger.e("DashboardViewModel", "Enable root failed", e.toString())
                _error.postValue("Enable root failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun disableRoot() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = rootManager.disableRoot()
                if (success) {
                    val status = rootManager.checkRootStatus()
                    _rootStatus.postValue(status)
                } else {
                    _error.postValue("Failed to disable root access")
                }
            } catch (e: Exception) {
                logger.e("DashboardViewModel", "Disable root failed", e.toString())
                _error.postValue("Disable root failed: ${e.message}")
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
                logger.e("DashboardViewModel", "Enable temp root failed", e.toString())
                _error.postValue("Enable temp root failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun disableTempRoot() {
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
                logger.e("DashboardViewModel", "Disable temp root failed", e.toString())
                _error.postValue("Disable temp root failed: ${e.message}")
            }
        }
    }

    fun enableHide() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = rootManager.enableHide()
                if (success) {
                    val status = rootManager.checkRootStatus()
                    _rootStatus.postValue(status)
                } else {
                    _error.postValue("Failed to enable root hide")
                }
            } catch (e: Exception) {
                logger.e("DashboardViewModel", "Enable hide failed", e.toString())
                _error.postValue("Enable hide failed: ${e.message}")
            }
        }
    }

    fun disableHide() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = rootManager.disableHide()
                if (success) {
                    val status = rootManager.checkRootStatus()
                    _rootStatus.postValue(status)
                } else {
                    _error.postValue("Failed to disable root hide")
                }
            } catch (e: Exception) {
                logger.e("DashboardViewModel", "Disable hide failed", e.toString())
                _error.postValue("Disable hide failed: ${e.message}")
            }
        }
    }

    fun setHideMode(mode: com.PixelSU.model.HideMode) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = rootManager.setHideMode(mode)
                if (success) {
                    val status = rootManager.checkRootStatus()
                    _rootStatus.postValue(status)
                } else {
                    _error.postValue("Failed to set hide mode")
                }
            } catch (e: Exception) {
                logger.e("DashboardViewModel", "Set hide mode failed", e.toString())
                _error.postValue("Set hide mode failed: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        rootManager.shutdown()
    }

    class Factory(
        private val rootManager: RootManager,
        private val logger: PixelSULogger
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(rootManager, logger) as T
        }
    }
}
