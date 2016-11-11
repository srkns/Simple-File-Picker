package com.simplemobiletools.filepicker.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v4.provider.DocumentFile
import android.widget.Toast
import com.simplemobiletools.filepicker.R
import java.io.File

fun Context.hasStoragePermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

fun Context.toast(id: Int) {
    Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
}

fun Context.getSDCardPath(): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        return ""
    }

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

fun Context.getHumanReadablePath(path: String): String {
    return getString(when (path) {
        "/" -> R.string.smtfp_root
        getInternalStoragePath() -> R.string.smtfp_internal
        else -> R.string.smtfp_sd_card
    })
}

fun Context.humanizePath(path: String): String {
    val basePath = path.getBasePath(this)
    return if (basePath == "/")
        "${getHumanReadablePath(basePath)}$path"
    else
        path.replaceFirst(basePath, getHumanReadablePath(basePath))
}

fun Context.getInternalStoragePath() = Environment.getExternalStorageDirectory().toString()

fun Context.isPathOnSD(path: String) = path.startsWith(getSDCardPath())

fun Context.isKitkatPlus() = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT

fun Context.needsStupidWritePermissions(path: String) = isPathOnSD(path) && isKitkatPlus() && !getSDCardPath().isEmpty()

fun Context.getFileDocument(path: String, treeUri: String): DocumentFile {
    val relativePath = path.substring(getSDCardPath().length + 1)
    var document = DocumentFile.fromTreeUri(this, Uri.parse(treeUri))
    val parts = relativePath.split("/")
    for (part in parts) {
        val currDocument = document.findFile(part)
        if (currDocument != null)
            document = currDocument
    }
    return document
}

fun Context.rescanItem(item: File) {
    if (item.isDirectory) {
        for (child in item.listFiles()) {
            rescanItem(child)
        }
    }

    MediaScannerConnection.scanFile(this, arrayOf(item.absolutePath), null, null)
}
