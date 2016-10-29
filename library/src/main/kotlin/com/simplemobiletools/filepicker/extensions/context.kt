package com.simplemobiletools.filepicker.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.widget.Toast
import java.io.File

fun Context.hasStoragePermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

fun Context.toast(id: Int) {
    Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
}

fun Context.getInternalPath() = Environment.getExternalStorageDirectory().toString()

fun Context.getSDCardPath(): String {
    val dirs = File("/storage").listFiles()
    for (dir in dirs) {
        try {
            if (Environment.isExternalStorageRemovable(dir))
                return dir.absolutePath
        } catch (e: Exception) {

        }
    }
    return ""
}
