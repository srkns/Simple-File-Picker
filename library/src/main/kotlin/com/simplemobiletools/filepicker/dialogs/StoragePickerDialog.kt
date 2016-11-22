package com.simplemobiletools.filepicker.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.filepicker.R
import com.simplemobiletools.filepicker.extensions.getBasePath
import com.simplemobiletools.filepicker.extensions.getInternalStoragePath
import com.simplemobiletools.filepicker.extensions.getSDCardPath

/**
 * A dialog for choosing between internal, root, SD card (optional) storage
 *
 * @param context has to be activity context to avoid some Theme.AppCompat issues
 * @param currPath current path to decide which storage should be preselected
 * @param callback an anonymous function
 *
 */
class StoragePickerDialog(val context: Context, currPath: String, val callback: (pickedPath: String) -> Unit) {
    var mDialog: AlertDialog?

    init {
        val inflater = LayoutInflater.from(context)
        val resources = context.resources
        val basePath = currPath.getBasePath(context)
        val layoutParams = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val radioGroup = inflater.inflate(R.layout.smtfp_radio_group, null) as RadioGroup

        val internalButton = inflater.inflate(R.layout.smtfp_radio_button, null) as RadioButton
        internalButton.apply {
            text = resources.getString(R.string.smtfp_internal)
            isChecked = basePath == context.getInternalStoragePath()
            setOnClickListener { internalPicked() }
        }
        radioGroup.addView(internalButton, layoutParams)

        if (isSDCardAvailable()) {
            val sdButton = inflater.inflate(R.layout.smtfp_radio_button, null) as RadioButton
            sdButton.apply {
                text = resources.getString(R.string.smtfp_sd_card)
                isChecked = basePath == context.getSDCardPath()
                setOnClickListener { sdPicked() }
            }
            radioGroup.addView(sdButton, layoutParams)
        }

        val rootButton = inflater.inflate(R.layout.smtfp_radio_button, null) as RadioButton
        rootButton.apply {
            text = resources.getString(R.string.smtfp_root)
            isChecked = basePath == "/"
            setOnClickListener { rootPicked() }
        }
        radioGroup.addView(rootButton, layoutParams)

        mDialog = AlertDialog.Builder(context)
                .setTitle(resources.getString(R.string.smtfp_select_storage))
                .setView(radioGroup)
                .create()

        mDialog?.show()
    }

    private fun internalPicked() {
        mDialog?.dismiss()
        callback.invoke(context.getInternalStoragePath())
    }

    private fun sdPicked() {
        mDialog?.dismiss()
        callback.invoke(context.getSDCardPath())
    }

    private fun rootPicked() {
        mDialog?.dismiss()
        callback.invoke("/")
    }

    private fun isSDCardAvailable() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            && !context.getSDCardPath().isEmpty()
}
