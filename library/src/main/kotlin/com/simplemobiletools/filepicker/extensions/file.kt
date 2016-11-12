package com.simplemobiletools.filepicker.extensions

import java.io.File

// fast extension check, not guaranteed to be accurate
fun File.isPhotoVideo(): Boolean {
    val photoVideoExtensions = arrayOf("jpg", "png", "jpeg", "gif", "bmp", "webp", "tiff",
            "gifv", "webm", "mkv", "flv", "vob", "avi", "wmv", "mp4", "ogv", "qt", "m4p", "mpg", "m4v", "mp2", "mpeg", "3gp")

    val filename = name.toLowerCase()
    for (ext in photoVideoExtensions) {
        if (filename.endsWith(ext))
            return true
    }
    return false
}
