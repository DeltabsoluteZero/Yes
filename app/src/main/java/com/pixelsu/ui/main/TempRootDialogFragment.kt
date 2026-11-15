package com.pixelsu.ui.main

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.pixelsu.R
import com.pixelsu.PixelSUApplication
import com.pixelsu.model.AppSettings
import com.pixelsu.root.RootManager
import com.pixelsu.utils.PixelSULogger

class TempRootDialogFragment : DialogFragment() {

    private lateinit var viewModel: TempRootViewModel
    private lateinit var app: PixelSUApplication
    private lateinit var rootManager: RootManager
    private lateinit var logger: PixelSULogger
    private var selectedDuration: Long = 10

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        app = requireActivity().application as PixelSUApplication
        rootManager = app.getRootManager()
        logger = app.getLogger()
        
        viewModel = ViewModelProvider(requireActivity(), TempRootViewModel.Factory(rootManager, logger)).get(TempRootViewModel::class.java)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_temp_root, null)
        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker_duration)
        
        numberPicker.minValue = 1
        numberPicker.maxValue = 1440
        numberPicker.value = 10
        numberPicker.wrapSelectorWheel = true
        numberPicker.setOnValueChangedListener { _, _, newVal -> selectedDuration = newVal.toLong() }

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_temp_root)
            .setMessage(R.string.temp_root_warning)
            .setView(view)
            .setPositiveButton(R.string.btn_enable_temp) { _, _ ->
                viewModel.enableTempRoot(selectedDuration)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
    }
}
