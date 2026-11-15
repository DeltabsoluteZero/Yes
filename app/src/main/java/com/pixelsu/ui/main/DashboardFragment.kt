package com.pixelsu.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.pixelsu.R
import com.pixelsu.common.AppComponent
import com.pixelsu.model.RootStatus
import com.pixelsu.root.RootManager
import com.pixelsu.utils.Logger
import com.pixelsu.utils.toast

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var app: AppComponent
    private lateinit var rootManager: RootManager
    private lateinit var logger: Logger

    private lateinit var btnEnableRoot: Button
    private lateinit var btnTempRoot: Button
    private lateinit var btnRootHide: Button
    private lateinit var btnExploits: Button
    private lateinit var btnLogs: Button
    private lateinit var btnRefresh: Button
    private lateinit var tvRootStatus: TextView
    private lateinit var tvRootType: TextView
    private lateinit var tvMagiskVersion: TextView
    private lateinit var tvKernelVersion: TextView
    private lateinit var tvSELinuxStatus: TextView
    private lateinit var tvDeviceInfo: TextView
    private lateinit var tvTempRootCountdown: TextView
    private lateinit var ivRootStatus: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        app = requireActivity().application as AppComponent
        rootManager = app.getRootManager()
        logger = app.getLogger()
        
        viewModel = ViewModelProvider(this, DashboardViewModel.Factory(rootManager, logger)).get(DashboardViewModel::class.java)

        btnEnableRoot = view.findViewById(R.id.btn_enable_root)
        btnTempRoot = view.findViewById(R.id.btn_temp_root)
        btnRootHide = view.findViewById(R.id.btn_root_hide)
        btnExploits = view.findViewById(R.id.btn_exploits)
        btnLogs = view.findViewById(R.id.btn_logs)
        btnRefresh = view.findViewById(R.id.btn_refresh)
        tvRootStatus = view.findViewById(R.id.tv_root_status)
        tvRootType = view.findViewById(R.id.tv_root_type)
        tvMagiskVersion = view.findViewById(R.id.tv_magisk_version)
        tvKernelVersion = view.findViewById(R.id.tv_kernel_version)
        tvSELinuxStatus = view.findViewById(R.id.tv_selinux_status)
        tvDeviceInfo = view.findViewById(R.id.tv_device_info)
        tvTempRootCountdown = view.findViewById(R.id.tv_temp_root_countdown)
        ivRootStatus = view.findViewById(R.id.iv_root_status)
        progressBar = view.findViewById(R.id.progress_bar)

        setupUI()
        observeViewModel()
        loadData()
    }

    private fun setupUI() {
        btnEnableRoot.setOnClickListener { viewModel.enableRoot() }
        btnTempRoot.setOnClickListener { showTempRootDialog() }
        btnRootHide.setOnClickListener { findNavController().navigate(R.id.nav_root_hide) }
        btnExploits.setOnClickListener { findNavController().navigate(R.id.nav_exploits) }
        btnLogs.setOnClickListener { findNavController().navigate(R.id.nav_logs) }
        btnRefresh.setOnClickListener { viewModel.refreshRootStatus() }
    }

    private fun observeViewModel() {
        viewModel.rootStatus.observe(viewLifecycleOwner, Observer { status ->
            updateUI(status)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnRefresh.isEnabled = !loading
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let { requireContext().toast(it) }
        })

        viewModel.success.observe(viewLifecycleOwner, Observer { success ->
            success?.let { requireContext().toast(it) }
        })
    }

    private fun loadData() {
        viewModel.refreshRootStatus()
    }

    private fun updateUI(status: RootStatus) {
        tvRootStatus.text = status.statusSummary
        
        val statusColor = when {
            status.isTempRootActive && !status.isTempRootExpired -> requireContext().getColor(R.color.warning)
            status.isTempRootActive && status.isTempRootExpired -> requireContext().getColor(R.color.error)
            status.isRooted && status.isHideActive -> requireContext().getColor(R.color.info)
            status.isRooted -> requireContext().getColor(R.color.success)
            else -> requireContext().getColor(R.color.error)
        }
        tvRootStatus.setTextColor(statusColor)

        tvRootType.text = getString(R.string.root_type_label, status.rootType.name)
        tvMagiskVersion.text = getString(R.string.magisk_version_label, status.magiskVersion)
        tvKernelVersion.text = getString(R.string.kernel_version_label, status.kernelVersion)
        tvSELinuxStatus.text = getString(R.string.selinux_status_label, status.selinuxStatus.name)
        tvDeviceInfo.text = getString(R.string.device_info_label, status.deviceModel, status.androidVersion, status.apiLevel)

        if (status.isTempRootActive) {
            tvTempRootCountdown.visibility = View.VISIBLE
            tvTempRootCountdown.text = getString(R.string.temp_root_countdown, formatDuration(status.tempRootRemaining))
        } else {
            tvTempRootCountdown.visibility = View.GONE
        }

        val iconRes = when {
            status.isTempRootActive && !status.isTempRootExpired -> R.drawable.ic_temp_root
            status.isRooted && status.isHideActive -> R.drawable.ic_root_hide
            status.isRooted -> R.drawable.ic_root_active
            else -> R.drawable.ic_root_inactive
        }
        ivRootStatus.setImageResource(iconRes)

        btnEnableRoot.isEnabled = !status.isRooted
        btnEnableRoot.text = if (status.isRooted) getString(R.string.root_already_enabled) else getString(R.string.btn_enable_root)
    }

    private fun showTempRootDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.btn_enable_temp))
            .setMessage(getString(R.string.temp_root_warning))
            .setPositiveButton(getString(R.string.btn_confirm)) { _, _ ->
                TempRootDialogFragment.newInstance().show(parentFragmentManager, "TempRootDialog")
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun formatDuration(millis: Long): String {
        val minutes = millis / 60000
        val seconds = (millis % 60000) / 1000
        return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
    }
}