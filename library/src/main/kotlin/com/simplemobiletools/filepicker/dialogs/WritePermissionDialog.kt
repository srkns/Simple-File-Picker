package com.simplemobiletools.filepicker.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.filepicker.R

/**
 * A dialog for displaying the steps needed to confirm SD card write access on Android 5+
 *
 * @param context: has to be activity context to avoid some Theme.AppCompat issues
 * @param callback: an anonymous function
 *
 */
class WritePermissionDialog(context: Context, val callback: () -> Unit) {
    var dialog: AlertDialog? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.smtfp_dialog_write_permission, null)

        dialog = AlertDialog.Builder(context)
                .setTitle(context.resources.getString(R.string.smtfp_confirm_storage_access_title))
                .setView(view)
                .setPositiveButton(R.string.smtfp_ok, { dialog, which -> dialogConfirmed() })
                .create()

        dialog?.show()
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback.invoke()
    }
}
