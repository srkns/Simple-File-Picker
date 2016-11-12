package com.simplemobiletools.filepicker.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.filepicker.R

class WritePermissionDialog(activity: Activity, val listener: OnConfirmedListener) {
    var dialog: AlertDialog? = null

    init {
        val view = LayoutInflater.from(activity).inflate(R.layout.smtfp_dialog_write_permission, null)

        dialog = AlertDialog.Builder(activity)
                .setTitle(activity.resources.getString(R.string.smtfp_confirm_storage_access_title))
                .setView(view)
                .setPositiveButton(R.string.smtfp_ok, { dialog, which -> dialogConfirmed() })
                .create()

        dialog?.show()
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        listener.onConfirmed()
    }

    interface OnConfirmedListener {
        fun onConfirmed()
    }
}
