package com.pixelsu.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import com.pixelsu.R

class SeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DialogPreference(context, attrs, defStyleAttr) {

    var currentProgress = 0
    var min = 0
    var max = 100

    init {
        layoutResource = R.layout.preference_dialog_seekbar
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            currentProgress = getPersistedInt(min)
        } else {
            currentProgress = defaultValue as? Int ?: min
            persistInt(currentProgress)
        }
    }

    @JvmName("setMinValue")
    fun setMin(min: Int) {
        this.min = min
    }

    @JvmName("setMaxValue")
    fun setMax(max: Int) {
        this.max = max
    }

    override fun getSummary(): CharSequence? {
        return "$currentProgress min"
    }
}

class SeekBarPreferenceDialogFragment : PreferenceDialogFragmentCompat() {

    override fun onCreateDialogView(context: android.content.Context): View {
        return View.inflate(context, R.layout.preference_dialog_seekbar, null)
    }

    override fun onBindDialogView(view: View) {
        val preference = getPreference() as? SeekBarPreference
            ?: return

        val seekBar = view.findViewById<SeekBar>(R.id.seekbar)
        val progressText = view.findViewById<TextView>(R.id.seekbar_progress)

        val min = preference.min
        val max = preference.max

        seekBar.max = max - min
        seekBar.progress = preference.currentProgress - min
        progressText.text = preference.currentProgress.toString()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newProgress = progress + min
                progressText.text = newProgress.toString()
                preference.currentProgress = newProgress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDialogClosed(positiveResult: Boolean) {
    }

    companion object {
        fun newInstance(key: String): SeekBarPreferenceDialogFragment {
            val fragment = SeekBarPreferenceDialogFragment()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }
}
