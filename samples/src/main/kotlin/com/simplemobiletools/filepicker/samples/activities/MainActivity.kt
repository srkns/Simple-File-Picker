package com.simplemobiletools.filepicker.samples.activities

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import com.simplemobiletools.filepicker.dialogs.PickFolderDialog
import com.simplemobiletools.filepicker.dialogs.PickFolderDialog.PickFolderResult.DISMISS
import com.simplemobiletools.filepicker.dialogs.PickFolderDialog.PickFolderResult.NO_PERMISSION
import com.simplemobiletools.filepicker.extensions.toast
import com.simplemobiletools.filepicker.samples.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pick_folder_button.setOnClickListener { pickFolder() }
    }

    private fun pickFolder() {
        val home = Environment.getExternalStorageDirectory().toString()

        PickFolderDialog(this, home, listener = object : PickFolderDialog.OnPickFolderListener {
            override fun onFail(error: PickFolderDialog.PickFolderResult) {
                when (error) {
                    NO_PERMISSION -> toast(R.string.no_permission)
                    DISMISS -> toast(R.string.dialog_dismissed)
                    else -> toast(R.string.unknown_error)
                }
            }

            override fun onSuccess(path: String) {
                picked_folder_path.text = path
            }
        })
    }
}
