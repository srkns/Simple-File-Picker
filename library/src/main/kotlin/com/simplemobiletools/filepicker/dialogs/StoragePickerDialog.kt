package com.simplemobiletools.filepicker.dialogs

import android.content.Context
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.filepicker.R

class StoragePickerDialog(context: Context) : android.app.AlertDialog.Builder(context), RadioGroup.OnCheckedChangeListener {
    val STORAGE_INTERNAL = 0
    val STORAGE_SD_CARD = 1
    val STORAGE_ROOT = 2

    var mContext: Context
    var mDialog: AlertDialog?

    init {
        mContext = context
        val inflater = LayoutInflater.from(mContext)
        val resources = context.resources

        val view = inflater.inflate(R.layout.smtfp_radio_group, null) as RadioGroup
        view.setOnCheckedChangeListener(this)

        val radioButton = inflater.inflate(R.layout.smtfp_radio_button, null) as RadioButton
        radioButton.apply {
            text = resources.getString(R.string.smtfp_internal)
            isChecked = false
            id = STORAGE_INTERNAL
        }
        view.addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        if (isSDCardAvailable()) {
            val sdButton = inflater.inflate(R.layout.smtfp_radio_button, null) as RadioButton
            sdButton.apply {
                text = resources.getString(R.string.smtfp_sd_card)
                isChecked = false
                id = STORAGE_SD_CARD
            }
            view.addView(sdButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        mDialog = AlertDialog.Builder(mContext)
                .setTitle(context.resources.getString(R.string.smtfp_select_storage))
                .setView(view)
                .create()

        mDialog?.show()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        mDialog?.dismiss()
    }

    private fun isSDCardAvailable() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}
