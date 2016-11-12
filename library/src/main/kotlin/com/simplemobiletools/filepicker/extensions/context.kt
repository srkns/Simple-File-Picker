package com.simplemobiletools.filepicker.extensions

import android.Manifest
import android.annotation.TargetApi
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
import java.util.*

fun Context.hasStoragePermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, id, length).show()

fun Context.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, msg, duration).show()

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

fun Context.scanFile(file: File, action: () -> Unit) {
    scanFiles(arrayListOf(file), action)
}

fun Context.scanPath(path: String, action: () -> Unit) {
    scanPaths(arrayListOf(path), action)
}

fun Context.scanFiles(files: ArrayList<File>, action: () -> Unit) {
    val allPaths = ArrayList<String>()
    for (file in files) {
        allPaths.addAll(getPaths(file))
    }
    rescanPaths(allPaths, action)
}

fun Context.scanPaths(paths: ArrayList<String>, action: () -> Unit) {
    val allPaths = ArrayList<String>()
    for (path in paths) {
        allPaths.addAll(getPaths(File(path)))
    }
    rescanPaths(allPaths, action)
}

fun Context.rescanPaths(paths: ArrayList<String>, action: () -> Unit) {
    var cnt = paths.size
    MediaScannerConnection.scanFile(this, paths.toTypedArray(), null, { s, uri ->
        if (--cnt == 0)
            action.invoke()
    })
}

fun getPaths(file: File): ArrayList<String> {
    val paths = ArrayList<String>()
    if (file.isDirectory) {
        val files = file.listFiles()
        for (curFile in files) {
            paths.addAll(getPaths(curFile))
        }
    } else {
        paths.add(file.absolutePath)
    }
    return paths
}
