package com.pixelsu.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.pixelsu.R
import com.pixelsu.common.AppComponent
import com.pixelsu.model.RootStatus
import com.pixelsu.root.RootManager
import com.pixelsu.utils.Logger
import com.pixelsu.utils.toast

class TempRootFragment : Fragment(R.layout.fragment_temp_root) {

    private lateinit var viewModel: TempRootViewModel
    private lateinit var app: AppComponent
    private lateinit var rootManager: RootManager
    private lateinit var logger: Logger

    private lateinit var btnEnableTempRoot: Button
    private lateinit var btnDisableTempRoot: Button
    private lateinit var seekBarDuration: SeekBar
    private lateinit var tvDurationValue: TextView
    private lateinit var tvTempRootStatus: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var progressBar: android.widget.ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as AppComponent
        rootManager = app.getRootManager()
        logger = app.getLogger()
        
        viewModel = ViewModelProvider(this, TempRootViewModel.Factory(rootManager, logger)).get(TempRootViewModel::class.java)

        btnEnableTempRoot = view.findViewById(R.id.btn_enable_temp_root)
        btnDisableTempRoot = view.findViewById(R.id.btn_disable_temp_root)
        seekBarDuration = view.findViewById(R.id.seek_bar_duration)
        tvDurationValue = view.findViewById(R.id.tv_duration_value)
        tvTempRootStatus = view.findViewById(R.id.tv_temp_root_status)
        tvCountdown = view.findViewById(R.id.tv_countdown)
        progressBar = view.findViewById(R.id.progress_bar)

        setupUI()
        observeViewModel()
        loadData()
    }

    private fun setupUI() {
        btnEnableTempRoot.setOnClickListener { 
            viewModel.enableTempRoot(seekBarDuration.progress)
        }
        
        btnDisableTempRoot.setOnClickListener { 
            viewModel.disableTempRoot()
        }
        
        seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDurationValue.text = getString(R.string.temp_root_duration_value, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        seekBarDuration.max = 120 // 2 hours max
        seekBarDuration.progress = 60 // Default 60 minutes
        tvDurationValue.text = getString(R.string.temp_root_duration_value, 60)
    }

    private fun observeViewModel() {
        viewModel.rootStatus.observe(viewLifecycleOwner, Observer { status ->
            updateUI(status)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnEnableTempRoot.isEnabled = !loading && !status?.isTempRootActive == true
            btnDisableTempRoot.isEnabled = !loading && status?.isTempRootActive == true
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let { requireContext().toast(it) }
        })

        viewModel.success.observe(viewLifecycleOwner, Observer { success ->
            success?.let { requireContext().toast(it) }
        })
    }

    private var status: RootStatus? = null

    private fun loadData() {
        viewModel.refreshTempRootStatus()
    }

    private fun updateUI(newStatus: RootStatus) {
        status = newStatus
        
        if (newStatus.isTempRootActive) {
            tvTempRootStatus.text = getString(R.string.temp_root_active)
            tvTempRootStatus.setTextColor(requireContext().getColor(R.color.success))
            
            tvCountdown.visibility = View.VISIBLE
            tvCountdown.text = getString(R.string.temp_root_countdown, formatDuration(newStatus.tempRootRemaining))
            
            btnEnableTempRoot.isEnabled = false
            btnDisableTempRoot.isEnabled = true
        } else {
            tvTempRootStatus.text = getString(R.string.temp_root_inactive)
            tvTempRootStatus.setTextColor(requireContext().getColor(R.color.text_secondary))
            
            tvCountdown.visibility = View.GONE
            
            btnEnableTempRoot.isEnabled = true
            btnDisableTempRoot.isEnabled = false
        }
    }

    private fun formatDuration(millis: Long): String {
        val minutes = millis / 60000
        val seconds = (millis % 60000) / 1000
        return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
    }
}