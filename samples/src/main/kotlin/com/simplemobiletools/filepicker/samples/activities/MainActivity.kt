package com.simplemobiletools.filepicker.samples.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.simplemobiletools.filepicker.dialogs.FilePickerDialog
import com.simplemobiletools.filepicker.dialogs.FilePickerDialog.FilePickerResult.DISMISS
import com.simplemobiletools.filepicker.dialogs.FilePickerDialog.FilePickerResult.NO_PERMISSION
import com.simplemobiletools.filepicker.extensions.toast
import com.simplemobiletools.filepicker.samples.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val STORAGE_PERMISSION = 1
    val PICK_FILE = 1
    val PICK_FOLDER = 2

    var action = PICK_FILE
    lateinit var home: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        home = Environment.getExternalStorageDirectory().toString()

        pick_file_button.setOnClickListener { openDialog(PICK_FILE) }
        pick_folder_button.setOnClickListener { openDialog(PICK_FOLDER) }
    }

    private fun openDialog(id: Int) {
        action = id
        if (!hasStoragePermission()) {
            requestStoragePermission()
            return
        }

        FilePickerDialog(this@MainActivity, home, pickFile = action == PICK_FILE, listener = object : FilePickerDialog.OnFilePickerListener {
            override fun onFail(error: FilePickerDialog.FilePickerResult) {
                when (error) {
                    NO_PERMISSION -> toast(R.string.no_permission)
                    DISMISS -> toast(R.string.dialog_dismissed)
                    else -> toast(R.string.unknown_error)
                }
            }

            override fun onSuccess(pickedPath: String) {
                if (action == PICK_FILE)
                    picked_file_path.text = pickedPath
                else
                    picked_folder_path.text = pickedPath
            }
        })
    }

    private fun requestStoragePermission() = ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION)
    private fun hasStoragePermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openDialog(action)
        }
    }
}
