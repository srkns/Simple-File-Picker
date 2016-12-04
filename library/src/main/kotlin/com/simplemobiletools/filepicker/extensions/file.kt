package com.simplemobiletools.filepicker.extensions

import android.media.MediaMetadataRetriever
import java.io.File

fun File.isImageVideoGif() = isImageFast() || isVideoFast() || isGif()

fun File.isGif() = name.toLowerCase().endsWith(".gif")

// fast extension check, not guaranteed to be accurate
fun File.isImageFast(): Boolean {
    val photoExtensions = arrayOf("jpg", "png", "jpeg", "bmp", "webp", "tiff")
    val filename = name.toLowerCase()
    return photoExtensions.any { filename.endsWith(it) }
}

fun File.isImageSlow() = isImageFast() || getMimeType(path).startsWith("image")

// fast extension check, not guaranteed to be accurate
fun File.isVideoFast(): Boolean {
    val videoExtensions = arrayOf("gifv", "webm", "mkv", "flv", "vob", "avi", "wmv", "mp4", "ogv", "qt", "m4p", "mpg", "m4v", "mp2", "mpeg", "3gp")
    val filename = name.toLowerCase()
    return videoExtensions.any { filename.endsWith(it) }
}

fun File.isVideoSlow() = isVideoFast() || getMimeType(path).startsWith("video")
fun File.isAudioSlow() = getMimeType(path).startsWith("audio")

fun getMimeType(path: String): String {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
    } catch (ignored: Exception) {
        ""
    }
}
