package com.pixelsu.ui.settings

import android.os.Bundle
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.pixelsu.R
import com.pixelsu.common.AppComponent
import com.pixelsu.model.AppSettings
import com.pixelsu.utils.Logger
import com.pixelsu.utils.toast

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var app: AppComponent
    private lateinit var logger: Logger

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        
        app = requireActivity().application as AppComponent
        logger = app.getLogger()
        
        viewModel = ViewModelProvider(this, SettingsViewModel.Factory(logger)).get(SettingsViewModel::class.java)

        setupPreferences()
        observeViewModel()
    }

    private fun setupPreferences() {
        // General
        findPreference<SwitchPreferenceCompat>("auto_start")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("autoStartOnBoot", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("biometric_lock")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("biometricLockEnabled", value)
            true
        }

        // Theme
        findPreference<Preference>("theme")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("theme", value)
            true
        }

        // Root settings
        findPreference<Preference>("magisk_path")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("magiskBinaryPath", value)
            true
        }

        findPreference<Preference>("shell_type")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("preferredShell", value)
            true
        }

        findPreference<Preference>("selinux_mode")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("selinuxMode", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("zygisk_enabled")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("zygiskEnabled", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("deny_list_mode")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("denyListMode", value)
            true
        }

        // Temp root
        findPreference<Preference>("temp_root_duration")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("defaultTempRootDuration", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("auto_disable_temp")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("autoDisableTempRoot", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("notify_temp_expiry")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("notifyTempRootExpiry", value)
            true
        }

        // UI settings
        findPreference<SwitchPreferenceCompat>("keep_screen_on")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("keepScreenOn", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("vibration_enabled")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("vibrationEnabled", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("sound_enabled")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("soundEnabled", value)
            true
        }

        // Log settings
        findPreference<Preference>("auto_clear_logs")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("autoClearLogsDays", value)
            true
        }

        findPreference<Preference>("max_log_entries")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("maxLogEntries", value)
            true
        }

        // Security
        findPreference<SwitchPreferenceCompat>("auth_for_root")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("requireAuthForRoot", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("auth_for_exploits")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("requireAuthForExploits", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("auth_for_hide")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("requireAuthForHide", value)
            true
        }

        findPreference<SwitchPreferenceCompat>("auth_for_settings")?.setOnPreferenceChangeListener { _, value ->
            viewModel.updateSetting("requireAuthForSettings", value)
            true
        }

        // Actions
        findPreference<Preference>("backup_config")?.setOnPreferenceClickListener {
            viewModel.backupConfig()
            true
        }

        findPreference<Preference>("restore_config")?.setOnPreferenceClickListener {
            viewModel.restoreConfig()
            true
        }

        findPreference<Preference>("clear_logs")?.setOnPreferenceClickListener {
            confirmClearLogs()
            true
        }

        findPreference<Preference>("export_logs")?.setOnPreferenceClickListener {
            exportLogs()
            true
        }

        // About
        findPreference<Preference>("about_version")?.summary = getString(R.string.about_version, BuildConfig.VERSION_NAME)
    }

    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let { requireContext().toast(it) }
        })

        viewModel.success.observe(viewLifecycleOwner, Observer { success ->
            success?.let { requireContext().toast(it) }
        })
    }

    private fun confirmClearLogs() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title_confirm))
            .setMessage(getString(R.string.dialog_msg_clear_logs))
            .setPositiveButton(getString(R.string.btn_confirm)) { _, _ ->
                viewModel.clearLogs()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun exportLogs() {
        viewModel.exportLogs()
    }
}