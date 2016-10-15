package com.simplemobiletools.filepicker.samples.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.FragmentActivity
import com.simplemobiletools.filepicker.dialogs.PickFolderDialog
import com.simplemobiletools.filepicker.samples.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity() {
    val PICK_FOLDER_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pick_folder_button.setOnClickListener { pickFolder() }
    }

    private fun pickFolder() {
        val home = Environment.getExternalStorageDirectory().toString()
        val dialog = PickFolderDialog.newInstance(home, true, true)
        dialog.requestCode = PICK_FOLDER_REQUEST_CODE
        dialog.show(supportFragmentManager, "pickfolder")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FOLDER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
