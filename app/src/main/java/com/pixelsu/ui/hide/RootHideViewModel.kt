package com.pixelsu.ui.hide

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pixelsu.hide.HideManager
import com.pixelsu.model.AppInfo
import com.pixelsu.model.HideListConfig
import com.pixelsu.model.HideListMode
import com.pixelsu.utils.PixelSULogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RootHideViewModel(
    private val hideManager: HideManager,
    private val logger: PixelSULogger
) : ViewModel() {

    private val _apps = MutableLiveData<List<AppInfo>>()
    val apps: LiveData<List<AppInfo>> = _apps

    private val _config = MutableLiveData<HideListConfig>()
    val config: LiveData<HideListConfig> = _config

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _hideStatus = MutableLiveData<Pair<Boolean, String>>()
    val hideStatus: LiveData<Pair<Boolean, String>> = _hideStatus

    private var allApps: List<AppInfo> = emptyList()

    init {
        hideManager.setListener(object : HideManager.HideListener {
            override fun onHideStatusChanged(hideActive: Boolean, mode: String) {
                _hideStatus.postValue(Pair(hideActive, mode))
            }

            override fun onAppListChanged(apps: List<AppInfo>) {
                allApps = apps
                applyFilter()
            }

            override fun onConfigChanged(config: HideListConfig) {
                _config.postValue(config)
            }
        })
    }

    fun loadApps() {
        _isLoading.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                hideManager.loadApps()
            } catch (e: Exception) {
                logger.e("RootHideViewModel", "Failed to load apps", e.toString())
                _error.postValue("Failed to load apps: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun checkHideStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val status = hideManager.checkHideStatus()
                _hideStatus.postValue(status)
            } catch (e: Exception) {
                logger.e("RootHideViewModel", "Failed to check hide status", e.toString())
            }
        }
    }

    fun enableHide() {
        _isLoading.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = hideManager.enableHide()
                if (!success) {
                    _error.postValue("Failed to enable root hide")
                }
            } catch (e: Exception) {
                logger.e("RootHideViewModel", "Enable hide failed", e.toString())
                _error.postValue("Enable hide failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun disableHide() {
        _isLoading.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = hideManager.disableHide()
                if (!success) {
                    _error.postValue("Failed to disable root hide")
                }
            } catch (e: Exception) {
                logger.e("RootHideViewModel", "Disable hide failed", e.toString())
                _error.postValue("Disable hide failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun setHideMode(mode: HideListMode) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                hideManager.setHideMode(mode)
            } catch (e: Exception) {
                logger.e("RootHideViewModel", "Set hide mode failed", e.toString())
                _error.postValue("Set hide mode failed: ${e.message}")
            }
        }
    }

    fun setIncludeSystemApps(include: Boolean) {
        val currentConfig = _config.value ?: HideListConfig()
        val newConfig = currentConfig.copy(
            includeSystemApps = include,
            lastModified = System.currentTimeMillis()
        )
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                hideManager.updateHideConfig(newConfig)
            } catch (e: Exception) {
                logger.e("RootHideViewModel", "Update config failed", e.toString())
                _error.postValue("Update config failed: ${e.message}")
            }
        }
    }

    fun toggleAppHide(packageName: String, checked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (checked) {
                    hideManager.addPackageToHideList(packageName)
                } else {
                    hideManager.removePackageFromHideList(packageName)
                }
            } catch (e: Exception) {
                logger.e("RootHideViewModel", "Toggle app hide failed", e.toString())
                _error.postValue("Toggle app hide failed: ${e.message}")
            }
        }
    }

    fun filterApps(query: String) {
        if (query.isBlank()) {
            _apps.postValue(allApps)
        } else {
            val filtered = hideManager.searchApps(query)
            _apps.postValue(filtered)
        }
    }

    private fun applyFilter() {
        // Current filter is handled by the search query
        val currentQuery = "" // Would need to track this
        if (currentQuery.isBlank()) {
            _apps.postValue(allApps)
        } else {
            _apps.postValue(hideManager.searchApps(currentQuery))
        }
    }

    class Factory(
        private val hideManager: HideManager,
        private val logger: PixelSULogger
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RootHideViewModel(hideManager, logger) as T
        }
    }
}
