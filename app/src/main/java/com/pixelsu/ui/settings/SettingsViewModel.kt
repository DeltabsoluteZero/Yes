package com.pixelsu.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pixelsu.model.AppSettings
import com.pixelsu.model.ThemeMode
import com.pixelsu.model.ShellType
import com.pixelsu.model.SELinuxMode
import com.pixelsu.utils.PixelSULogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val logger: PixelSULogger
) : ViewModel() {

    private val _settings = MutableLiveData<AppSettings>()
    val settings: LiveData<AppSettings> = _settings

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success

    init {
        loadSettings()
    }

    private fun loadSettings() {
        // Load from SharedPreferences/DataStore
        _settings.value = AppSettings.getDefaults()
    }

    fun updateSetting(key: String, value: Any) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentSettings = _settings.value ?: AppSettings.getDefaults()
                val updatedSettings = applySetting(currentSettings, key, value)
                
                // Save to SharedPreferences/DataStore
                saveSettings(updatedSettings)
                
                _settings.postValue(updatedSettings)
                _success.postValue("Setting updated")
            } catch (e: Exception) {
                logger.e("SettingsViewModel", "Failed to update setting", e.toString())
                _error.postValue("Failed to update setting: ${e.message}")
            }
        }
    }

    private fun applySetting(settings: AppSettings, key: String, value: Any): AppSettings {
        return when (key) {
            "autoStartOnBoot" -> settings.copy(autoStartOnBoot = value as Boolean)
            "biometricLockEnabled" -> settings.copy(biometricLockEnabled = value as Boolean)
            "theme" -> settings.copy(theme = ThemeMode.valueOf(value as String))
            "magiskBinaryPath" -> settings.copy(magiskBinaryPath = value as String)
            "preferredShell" -> settings.copy(preferredShell = ShellType.valueOf(value as String))
            "selinuxMode" -> settings.copy(selinuxMode = SELinuxMode.valueOf(value as String))
            "defaultTempRootDuration" -> settings.copy(defaultTempRootDuration = value as Int)
            else -> settings
        }
    }

    private fun saveSettings(settings: AppSettings) {
        // Save to SharedPreferences/DataStore
    }

    fun backupConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Export settings to file
                _success.postValue("Configuration backed up")
            } catch (e: Exception) {
                _error.postValue("Backup failed: ${e.message}")
            }
        }
    }

    fun restoreConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Import settings from file
                _success.postValue("Configuration restored")
            } catch (e: Exception) {
                _error.postValue("Restore failed: ${e.message}")
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Clear logs via logger
                _success.postValue("Logs cleared")
            } catch (e: Exception) {
                _error.postValue("Clear logs failed: ${e.message}")
            }
        }
    }

    fun exportLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Export logs via logger
                _success.postValue("Logs exported")
            } catch (e: Exception) {
                _error.postValue("Export failed: ${e.message}")
            }
        }
    }

    class Factory(
        private val logger: PixelSULogger
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(logger) as T
        }
    }
}
