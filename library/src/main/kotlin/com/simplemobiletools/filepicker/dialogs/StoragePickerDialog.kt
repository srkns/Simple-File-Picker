package com.simplemobiletools.filepicker.dialogs

import android.content.Context
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.filepicker.R
import com.simplemobiletools.filepicker.extensions.getInternalPath
import com.simplemobiletools.filepicker.extensions.getSDCardPath

class StoragePickerDialog(context: Context, val basePath: String, val listener: OnStoragePickerListener) : AlertDialog.Builder(context) {
    interface OnStoragePickerListener {
        fun onPick(path: String)
    }

    var mDialog: AlertDialog?

    init {
        val inflater = LayoutInflater.from(context)
        val resources = context.resources

        val radioGroup = inflater.inflate(R.layout.smtfp_radio_group, null) as RadioGroup
        val internalButton = inflater.inflate(R.layout.smtfp_radio_button, null) as RadioButton
        internalButton.apply {
            text = resources.getString(R.string.smtfp_internal)
            isChecked = basePath == context.getInternalPath()
            setOnClickListener { internalPicked() }
        }
        radioGroup.addView(internalButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        if (isSDCardAvailable()) {
            val sdButton = inflater.inflate(R.layout.smtfp_radio_button, null) as RadioButton
            sdButton.apply {
                text = resources.getString(R.string.smtfp_sd_card)
                isChecked = basePath == context.getSDCardPath()
                setOnClickListener { sdPicked() }
            }
            radioGroup.addView(sdButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        mDialog = AlertDialog.Builder(context)
                .setTitle(context.resources.getString(R.string.smtfp_select_storage))
                .setView(radioGroup)
                .create()

        mDialog?.show()
    }

    private fun internalPicked() {
        mDialog?.dismiss()
        listener.onPick(context.getInternalPath())
    }

    private fun sdPicked() {
        mDialog?.dismiss()
        listener.onPick(context.getSDCardPath())
    }

    private fun isSDCardAvailable() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}
