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

class StoragePickerDialog(context: Context, val basePath: String, val listener: OnStoragePickerListener) : AlertDialog.Builder(context), RadioGroup.OnCheckedChangeListener {
    interface OnStoragePickerListener {
        fun onPick(path: String)
    }

    val STORAGE_INTERNAL = 0
    val STORAGE_SD_CARD = 1
    val STORAGE_ROOT = 2

    var mDialog: AlertDialog?

    init {
        val inflater = LayoutInflater.from(context)
        val resources = context.resources

        val view = inflater.inflate(R.layout.smtfp_radio_group, null) as RadioGroup

        val radioButton = inflater.inflate(R.layout.smtfp_radio_button, null) as RadioButton
        radioButton.apply {
            text = resources.getString(R.string.smtfp_internal)
            isChecked = basePath == context.getInternalPath()
            id = STORAGE_INTERNAL
        }
        view.addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        if (isSDCardAvailable()) {
            val sdButton = inflater.inflate(R.layout.smtfp_radio_button, null) as RadioButton
            sdButton.apply {
                text = resources.getString(R.string.smtfp_sd_card)
                isChecked = basePath == context.getSDCardPath()
                id = STORAGE_SD_CARD
            }
            view.addView(sdButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        view.setOnCheckedChangeListener(this)
        mDialog = AlertDialog.Builder(context)
                .setTitle(context.resources.getString(R.string.smtfp_select_storage))
                .setView(view)
                .create()

        mDialog?.show()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        mDialog?.dismiss()
        listener.onPick(if (checkedId == STORAGE_INTERNAL) context.getInternalPath() else context.getSDCardPath())
    }

    private fun isSDCardAvailable() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}
