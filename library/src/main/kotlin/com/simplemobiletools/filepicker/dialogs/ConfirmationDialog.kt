package com.simplemobiletools.filepicker.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import com.simplemobiletools.filepicker.R

/**
 * A simple dialog without any view, just a message, a positive button and optionally a negative button
 *
 * @param context: has to be activity context to avoid some Theme.AppCompat issues
 * @param message: the dialogs message ID
 * @param positive: positive buttons text ID
 * @param negative: negative buttons text ID (optional)
 * @param listener: callback listening for positive button press
 */
class ConfirmationDialog(context: Context, message: Int = R.string.smtfp_proceed_with_deletion, positive: Int = R.string.smtfp_yes,
                         negative: Int = R.string.smtfp_no, val listener: OnConfirmedListener) {
    var dialog: AlertDialog? = null

    init {
        val builder = AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(positive, { dialog, which -> dialogConfirmed() })

        if (negative != 0)
            builder.setNegativeButton(negative, null)

        dialog = builder.create()
        dialog!!.show()
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        listener.onConfirmed()
    }

    interface OnConfirmedListener {
        fun onConfirmed()
    }
}
