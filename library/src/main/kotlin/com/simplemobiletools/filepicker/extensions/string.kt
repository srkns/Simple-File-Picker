package com.simplemobiletools.filepicker.extensions

import android.content.Context
import java.util.regex.Pattern

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

fun String.getFilenameExtension() = substring(lastIndexOf(".") + 1)

fun String.getBasePath(context: Context): String {
    return if (startsWith(context.getInternalStoragePath()))
        context.getInternalStoragePath()
    else if (!context.getSDCardPath().isEmpty() && startsWith(context.getSDCardPath()))
        context.getSDCardPath()
    else
        "/"
}

fun String.isAValidFilename(): Boolean {
    val pattern = Pattern.compile("^[-_.A-Za-z0-9()#& ]+$")
    val matcher = pattern.matcher(this)
    return matcher.matches()
}
