package com.simplemobiletools.filepicker.asynctasks

import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.provider.MediaStore
import android.support.v4.util.Pair
import android.util.Log
import com.simplemobiletools.filepicker.extensions.*
import java.io.*
import java.lang.ref.WeakReference
import java.util.*

class CopyMoveTask(val activity: Context, val deleteAfterCopy: Boolean = false, val treeUri: String = "", val copyMediaOnly: Boolean,
                   listener: CopyMoveTask.CopyMoveListener) : AsyncTask<Pair<ArrayList<File>, File>, Void, Boolean>() {
    private val TAG = CopyMoveTask::class.java.simpleName
    private var mListener: WeakReference<CopyMoveListener>? = null
    private var mMovedFiles: ArrayList<File> = ArrayList()
    private var mNewFiles: ArrayList<File> = ArrayList()
    lateinit var mFiles: ArrayList<File>

    init {
        mListener = WeakReference(listener)
    }

    override fun doInBackground(vararg params: Pair<ArrayList<File>, File>): Boolean? {
        val pair = params[0]
        mFiles = pair.first

        for (file in mFiles) {
            try {
                val curFile = File(pair.second, file.name)
                if (curFile.exists())
                    continue

                copy(file, curFile)
            } catch (e: Exception) {
                Log.e(TAG, "copy $e")
                return false
            }
        }

        if (deleteAfterCopy) {
            for (file in mMovedFiles) {
                if (activity.needsStupidWritePermissions(file.absolutePath)) {
                    activity.getFileDocument(file.absolutePath, treeUri).delete()
                } else {
                    file.delete()
                }
            }
        }
        activity.scanFiles(mFiles) {}
        activity.scanFiles(mMovedFiles) {}

        for (file in mNewFiles) {
            val filesUri = MediaStore.Files.getContentUri("external")

            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            values.put(MediaStore.MediaColumns.SIZE, file.length())
            activity.contentResolver.insert(filesUri, values)
        }

        return true
    }

    @Throws(Exception::class)
    private fun copy(source: File, destination: File) {
        if (source.isDirectory) {
            copyDirectory(source, destination)
        } else {
            copyFile(source, destination)
        }
    }

    private fun copyDirectory(source: File, destination: File) {
        if (!destination.exists()) {
            if (activity.needsStupidWritePermissions(destination.absolutePath)) {
                val document = activity.getFileDocument(destination.absolutePath, treeUri)
                document.createDirectory(destination.name)
            } else if (!destination.mkdirs()) {
                throw IOException("Could not create dir ${destination.absolutePath}")
            }
        }

        val children = source.list()
        for (child in children) {
            val newFile = File(destination, child)
            if (newFile.exists())
                continue

            val curFile = File(source, child)
            if (activity.needsStupidWritePermissions(destination.absolutePath)) {
                if (newFile.isDirectory) {
                    copyDirectory(curFile, newFile)
                } else {
                    copyFile(curFile, newFile)
                }
            } else {
                copy(curFile, newFile)
            }
        }
    }

    private fun copyFile(source: File, destination: File) {
        if (copyMediaOnly && !source.isImageVideoGif())
            return

        val directory = destination.parentFile
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Could not create dir ${directory.absolutePath}")
        }

        val inputStream = FileInputStream(source)
        val out: OutputStream?
        if (activity.needsStupidWritePermissions(destination.absolutePath)) {
            var document = activity.getFileDocument(destination.absolutePath, treeUri)
            document = document.createFile("", destination.name)

            out = activity.contentResolver.openOutputStream(document.uri)
        } else {
            out = FileOutputStream(destination)
        }

        copyStream(inputStream, out)
        activity.scanFile(destination) {}
        mMovedFiles.add(source)
        mNewFiles.add(destination)
    }

    private fun copyStream(inputStream: InputStream, out: OutputStream?) {
        val buf = ByteArray(1024)
        var len: Int
        while (true) {
            len = inputStream.read(buf)
            if (len <= 0)
                break
            out?.write(buf, 0, len)
        }
    }

    override fun onPostExecute(success: Boolean) {
        val listener = mListener?.get() ?: return

        if (success) {
            listener.copySucceeded(deleteAfterCopy, mFiles.size == mMovedFiles.size)
        } else {
            listener.copyFailed()
        }
    }

    interface CopyMoveListener {
        fun copySucceeded(deleted: Boolean, copiedAll: Boolean)

        fun copyFailed()
    }
}
