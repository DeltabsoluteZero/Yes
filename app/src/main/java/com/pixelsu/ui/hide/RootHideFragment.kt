package com.pixelsu.ui.hide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pixelsu.R
import com.pixelsu.common.AppComponent
import com.pixelsu.hide.HideManager
import com.pixelsu.model.AppInfo
import com.pixelsu.model.HideListConfig
import com.pixelsu.model.HideListMode
import com.pixelsu.utils.Logger
import com.pixelsu.utils.toast

class RootHideFragment : Fragment(R.layout.fragment_root_hide) {

    private lateinit var viewModel: RootHideViewModel
    private lateinit var app: AppComponent
    private lateinit var hideManager: HideManager
    private lateinit var logger: Logger
    private lateinit var adapter: AppHideAdapter

    private lateinit var switchHideRoot: Switch
    private lateinit var switchSystemApps: Switch
    private lateinit var spinnerMode: Spinner
    private lateinit var etSearch: androidx.appcompat.widget.AppCompatEditText
    private lateinit var tvHideStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as AppComponent
        hideManager = app.getHideManager()
        logger = app.getLogger()
        
        viewModel = ViewModelProvider(this, RootHideViewModel.Factory(hideManager, logger)).get(RootHideViewModel::class.java)

        switchHideRoot = view.findViewById(R.id.switch_hide_root)
        switchSystemApps = view.findViewById(R.id.switch_system_apps)
        spinnerMode = view.findViewById(R.id.spinner_mode)
        etSearch = view.findViewById(R.id.et_search)
        tvHideStatus = view.findViewById(R.id.tv_hide_status)
        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.recycler_view)

        setupUI()
        observeViewModel()
        loadData()
    }

    private fun setupUI() {
        adapter = AppHideAdapter(hideConfig = viewModel.config.value ?: com.pixelsu.model.HideListConfig(), onAppToggle = { appInfo, checked ->
            viewModel.toggleAppHide(appInfo.packageName, checked)
        })
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        spinnerMode.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            HideListMode.values().map { it.name.replace("_", " ") }
        )
        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val mode = HideListMode.values()[position]
                viewModel.setHideMode(mode)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        switchHideRoot.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.enableHide()
            } else {
                viewModel.disableHide()
            }
        }

        switchSystemApps.setOnCheckedChangeListener { _, checked ->
            viewModel.setIncludeSystemApps(checked)
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterApps(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.apps.observe(viewLifecycleOwner, Observer { apps ->
            adapter.submitList(apps)
        })

        viewModel.config.observe(viewLifecycleOwner, Observer { config ->
            updateUI(config)
            adapter.updateHideConfig(config)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let { requireContext().toast(it) }
        })

        viewModel.hideStatus.observe(viewLifecycleOwner, Observer { status ->
            switchHideRoot.isChecked = status.first
            tvHideStatus.text = if (status.first) "Active: ${status.second}" else "Inactive"
            tvHideStatus.setTextColor(requireContext().getColor(if (status.first) R.color.success else R.color.error))
        })
    }

    private fun loadData() {
        viewModel.loadApps()
        viewModel.checkHideStatus()
    }

    private fun updateUI(config: HideListConfig) {
        switchSystemApps.isChecked = config.includeSystemApps
        
        val modeIndex = HideListMode.values().indexOf(config.mode)
        spinnerMode.setSelection(modeIndex)
    }
}