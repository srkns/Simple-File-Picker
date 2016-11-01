package com.simplemobiletools.filepicker.extensions

import android.content.Context

fun String.getFilenameFromPath(): String {
    return substring(lastIndexOf("/") + 1)
}

fun String.getBasePath(context: Context): String {
    return if (this.startsWith(context.getInternalStoragePath()))
        context.getInternalStoragePath()
    else if (!context.getSDCardPath().isEmpty() && this.startsWith(context.getSDCardPath()))
        context.getSDCardPath()
    else
        "/"
}
