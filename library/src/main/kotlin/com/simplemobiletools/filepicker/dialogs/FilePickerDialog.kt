package com.simplemobiletools.filepicker.dialogs

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import com.simplemobiletools.filepicker.R
import com.simplemobiletools.filepicker.adapters.ItemsAdapter
import com.simplemobiletools.filepicker.extensions.getFilenameFromPath
import com.simplemobiletools.filepicker.extensions.hasStoragePermission
import com.simplemobiletools.filepicker.models.FileDirItem
import com.simplemobiletools.filepicker.views.Breadcrumbs
import kotlinx.android.synthetic.main.smtfp_directory_picker.view.*
import java.io.File
import java.util.*
import kotlin.comparisons.compareBy

class FilePickerDialog() : Breadcrumbs.BreadcrumbsListener {

    interface OnFilePickerListener {
        fun onFail(error: FilePickerResult)

        fun onSuccess(path: String)
    }

    enum class FilePickerResult() {
        NO_PERMISSION, DISMISS
    }

    var mPath = ""
    var mShowHidden = false
    var mShowFullPath = false
    var mListener: OnFilePickerListener? = null

    var mFirstUpdate = true
    var mPickFile = true
    lateinit var mContext: Context
    lateinit var mDialog: AlertDialog
    lateinit var mDialogView: View

    /**
     * The only filepicker constructor with a couple optional parameters
     *
     * @param activity use the activity instead of any context to avoid some Theme.AppCompat issues
     * @param path initial path of the dialog, defaults to the external storage
     * @param listener the callback used for returning the success or failure result to the initiator
     * @param pickFile toggle used to determine if we are picking a file or a folder
     * @param showHidden toggle for showing hidden items, whose name starts with a dot
     * @param showFullPath show the full path in the breadcrumb, i.e. "/storage/emulated/0" instead of "home"
     */
    constructor(activity: Activity,
                path: String = Environment.getExternalStorageDirectory().toString(),
                pickFile: Boolean = true,
                showHidden: Boolean = false,
                showFullPath: Boolean = false,
                listener: OnFilePickerListener) : this() {
        mContext = activity
        mPath = path
        mShowHidden = showHidden
        mShowFullPath = showFullPath
        mListener = listener
        mPickFile = pickFile

        if (!mContext.hasStoragePermission()) {
            mListener?.onFail(FilePickerResult.NO_PERMISSION)
            return
        }

        mDialogView = LayoutInflater.from(mContext).inflate(R.layout.smtfp_directory_picker, null)
        updateItems()
        setupBreadcrumbs()

        // if a dialog's listview has height wrap_content, it calls getView way too often which can reduce performance
        mDialogView.directory_picker_list.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mDialogView.directory_picker_list.layoutParams.height = mDialogView.directory_picker_list.height
                mDialogView.directory_picker_list.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })


        val builder = AlertDialog.Builder(mContext)
                .setTitle(getTitle())
                .setView(mDialogView)
                .setNegativeButton(R.string.smtfp_cancel, { dialog, which -> dialogDismissed() })
                .setOnCancelListener({ dialogDismissed() })

        if (!mPickFile)
            builder.setPositiveButton(R.string.smtfp_ok) { dialog, which -> sendSuccess() }

        mDialog = builder.create()
        mDialog.show()
    }

    private fun getTitle() = mContext.resources.getString(if (mPickFile) R.string.smtfp_select_file else R.string.smtfp_select_folder)

    private fun dialogDismissed() {
        mListener?.onFail(FilePickerResult.DISMISS)
    }

    private fun updateItems() {
        var items = getItems(mPath)
        if (!containsDirectory(items) && !mFirstUpdate && !mPickFile) {
            sendSuccess()
            return
        }

        items = items.sortedWith(compareBy({ !it.isDirectory }, { it.name.toLowerCase() }))

        val adapter = ItemsAdapter(mContext, items)
        mDialogView.directory_picker_list.adapter = adapter
        mDialogView.directory_picker_breadcrumbs.setBreadcrumb(mPath, mShowFullPath)
        mDialogView.directory_picker_list.setOnItemClickListener { adapterView, view, position, id ->
            val item = items[position]
            if (item.isDirectory) {
                mPath = item.path
                updateItems()
            } else if (mPickFile) {
                mPath = item.path
                sendSuccess()
            }
        }

        mFirstUpdate = false
    }

    private fun sendSuccess() {
        val file = File(mPath)
        if ((mPickFile && file.isFile) || (!mPickFile && file.isDirectory)) {
            mListener?.onSuccess(mPath)
            mDialog.dismiss()
        }
    }

    private fun setupBreadcrumbs() {
        mDialogView.directory_picker_breadcrumbs.setListener(this)
    }

    private fun getItems(path: String): List<FileDirItem> {
        val items = ArrayList<FileDirItem>()
        val base = File(path)
        val files = base.listFiles()
        if (files != null) {
            for (file in files) {
                if (!mShowHidden && file.isHidden)
                    continue

                val curPath = file.absolutePath
                val curName = curPath.getFilenameFromPath()
                val size = file.length()
                items.add(FileDirItem(curPath, curName, file.isDirectory, getChildren(file), size))
            }
        }
        return items
    }

    private fun getChildren(file: File): Int {
        if (file.listFiles() == null || !file.isDirectory)
            return 0

        return file.listFiles().size
    }

    private fun containsDirectory(items: List<FileDirItem>): Boolean {
        for (item in items) {
            if (item.isDirectory) {
                return true
            }
        }
        return false
    }

    override fun breadcrumbClicked(id: Int) {
        val item = mDialogView.directory_picker_breadcrumbs.getChildAt(id).tag as FileDirItem
        mPath = item.path
        updateItems()
    }
}
