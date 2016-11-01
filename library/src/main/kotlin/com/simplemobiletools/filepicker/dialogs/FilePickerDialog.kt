package com.simplemobiletools.filepicker.dialogs

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import com.simplemobiletools.filepicker.R
import com.simplemobiletools.filepicker.adapters.ItemsAdapter
import com.simplemobiletools.filepicker.extensions.getFilenameFromPath
import com.simplemobiletools.filepicker.extensions.hasStoragePermission
import com.simplemobiletools.filepicker.extensions.toast
import com.simplemobiletools.filepicker.models.FileDirItem
import com.simplemobiletools.filepicker.views.Breadcrumbs
import kotlinx.android.synthetic.main.smtfp_directory_picker.view.*
import java.io.File
import java.util.*
import kotlin.comparisons.compareBy

/**
 * The only filepicker constructor with a couple optional parameters
 *
 * @param activity use the activity instead of any context to avoid some Theme.AppCompat issues
 * @param currPath initial path of the dialog, defaults to the external storage
 * @param pickFile toggle used to determine if we are picking a file or a folder
 * @param showHidden toggle for showing hidden items, whose name starts with a dot
 * @param mustBeWritable toggle to allow picking only files or directories that can be modified
 * @param listener the callback used for returning the success or failure result to the initiator
 */
class FilePickerDialog(val activity: Activity,
                       var currPath: String = Environment.getExternalStorageDirectory().toString(),
                       val pickFile: Boolean = true,
                       val showHidden: Boolean = false,
                       val mustBeWritable: Boolean = true,
                       val listener: OnFilePickerListener) : Breadcrumbs.BreadcrumbsListener {

    var mFirstUpdate = true
    var mContext: Context
    lateinit var mDialog: AlertDialog
    lateinit var mDialogView: View

    init {
        mContext = activity

        if (!mContext.hasStoragePermission()) {
            listener.onFail(FilePickerResult.NO_PERMISSION)
        } else {
            mDialogView = LayoutInflater.from(mContext).inflate(R.layout.smtfp_directory_picker, null)
            updateItems()
            setupBreadcrumbs()

            // if a dialog's listview has height wrap_content, it calls getView way too often which can reduce performance
            // lets just measure it, then set a static height
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
                    .setOnKeyListener({ dialogInterface, i, keyEvent ->
                        if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                            val breadcrumbs = mDialogView.directory_picker_breadcrumbs
                            if (breadcrumbs.childCount > 1) {
                                breadcrumbs.removeBreadcrumb()
                                currPath = breadcrumbs.lastItem.path
                                updateItems()
                            } else {
                                mDialog.dismiss()
                                dialogDismissed()
                            }
                        }
                        true
                    })

            if (!pickFile)
                builder.setPositiveButton(R.string.smtfp_ok, null)

            mDialog = builder.create()
            mDialog.show()

            if (!pickFile) {
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                    verifyPath()
                })
            }
        }
    }

    private fun getTitle() = mContext.resources.getString(if (pickFile) R.string.smtfp_select_file else R.string.smtfp_select_folder)

    private fun dialogDismissed() {
        listener.onFail(FilePickerResult.DISMISS)
    }

    private fun updateItems() {
        var items = getItems(currPath)
        if (!containsDirectory(items) && !mFirstUpdate && !pickFile) {
            verifyPath()
            return
        }

        items = items.sortedWith(compareBy({ !it.isDirectory }, { it.name.toLowerCase() }))

        val adapter = ItemsAdapter(mContext, items)
        mDialogView.directory_picker_list.adapter = adapter
        mDialogView.directory_picker_breadcrumbs.setBreadcrumb(currPath)
        mDialogView.directory_picker_list.setOnItemClickListener { adapterView, view, position, id ->
            val item = items[position]
            if (item.isDirectory) {
                currPath = item.path
                updateItems()
            } else if (pickFile) {
                currPath = item.path
                verifyPath()
            }
        }

        mFirstUpdate = false
    }

    private fun verifyPath() {
        val file = File(currPath)
        if (pickFile && file.isFile) {
            if (mustBeWritable && !file.canWrite()) {
                mContext.toast(R.string.smtfp_file_not_writable)
            } else {
                sendSuccess()
            }
        } else if (!pickFile && file.isDirectory) {
            if (mustBeWritable && !file.canWrite()) {
                mContext.toast(R.string.smtfp_dir_not_writable)
            } else {
                sendSuccess()
            }
        }
    }

    private fun sendSuccess() {
        listener.onSuccess(currPath)
        mDialog.dismiss()
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
                if (!showHidden && file.isHidden)
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
        if (id == 0) {
            StoragePickerDialog(activity, currPath, object : StoragePickerDialog.OnStoragePickerListener {
                override fun onPick(pickedPath: String) {
                    currPath = pickedPath
                    updateItems()
                }
            })
        } else {
            val item = mDialogView.directory_picker_breadcrumbs.getChildAt(id).tag as FileDirItem
            currPath = item.path
            updateItems()
        }
    }

    interface OnFilePickerListener {
        fun onFail(error: FilePickerResult)

        fun onSuccess(pickedPath: String)
    }

    enum class FilePickerResult() {
        NO_PERMISSION, DISMISS
    }
}
