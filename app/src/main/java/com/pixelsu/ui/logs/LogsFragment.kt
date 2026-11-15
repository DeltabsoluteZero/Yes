package com.pixelsu.ui.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pixelsu.R
import com.pixelsu.common.AppComponent
import com.pixelsu.model.LogEntry
import com.pixelsu.utils.Logger
import com.pixelsu.utils.toast

class LogsFragment : Fragment(R.layout.fragment_logs) {

    private lateinit var viewModel: LogsViewModel
    private lateinit var app: AppComponent
    private lateinit var logger: Logger
    private lateinit var adapter: LogAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as AppComponent
        logger = app.getLogger()
        
        viewModel = ViewModelProvider(this, LogsViewModel.Factory(logger, requireContext())).get(LogsViewModel::class.java)

        setupRecyclerView(view)
        observeViewModel()
        loadLogs()
    }

    private fun setupRecyclerView(rootView: View) {
        adapter = LogAdapter()
        val recyclerView = rootView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val btnClearLogs = rootView.findViewById<Button>(R.id.btn_clear_logs)
        btnClearLogs.setOnClickListener {
            confirmClearLogs()
        }

        val btnExportLogs = rootView.findViewById<Button>(R.id.btn_export_logs)
        btnExportLogs.setOnClickListener {
            exportLogs()
        }
    }

    private fun observeViewModel() {
        viewModel.logs.observe(viewLifecycleOwner, Observer { logs ->
            adapter.submitList(logs)
            view?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_view)?.scrollToPosition(0)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { loading ->
            // Handle loading state
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let { requireContext().toast(it) }
        })

        viewModel.success.observe(viewLifecycleOwner, Observer { success ->
            success?.let { requireContext().toast(it) }
        })
    }

    private fun loadLogs() {
        viewModel.loadLogs()
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