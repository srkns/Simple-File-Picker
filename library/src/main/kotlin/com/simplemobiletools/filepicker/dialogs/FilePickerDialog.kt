package com.simplemobiletools.filepicker.dialogs

import android.content.Context
import android.graphics.Rect
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import com.simplemobiletools.filepicker.R
import com.simplemobiletools.filepicker.adapters.ItemsAdapter
import com.simplemobiletools.filepicker.extensions.getFilenameFromPath
import com.simplemobiletools.filepicker.extensions.getInternalStoragePath
import com.simplemobiletools.filepicker.extensions.hasStoragePermission
import com.simplemobiletools.filepicker.models.FileDirItem
import com.simplemobiletools.filepicker.views.Breadcrumbs
import com.simplemobiletools.filepicker.views.RecyclerViewDivider
import kotlinx.android.synthetic.main.smtfp_directory_picker.view.*
import java.io.File
import java.util.*
import kotlin.comparisons.compareBy

/**
 * The only filepicker constructor with a couple optional parameters
 *
 * @param context: has to be activity context to avoid some Theme.AppCompat issues
 * @param currPath initial path of the dialog, defaults to the external storage
 * @param pickFile toggle used to determine if we are picking a file or a folder
 * @param showHidden toggle for showing hidden items, whose name starts with a dot
 * @param listener the callback used for returning the success or failure result to the initiator
 */
class FilePickerDialog(val context: Context,
                       var currPath: String = Environment.getExternalStorageDirectory().toString(),
                       val pickFile: Boolean = true,
                       val showHidden: Boolean = false,
                       val listener: OnFilePickerListener) : Breadcrumbs.BreadcrumbsListener {

    var mFirstUpdate = true
    lateinit var mDialog: AlertDialog
    lateinit var mDialogView: View

    init {
        if (!context.hasStoragePermission()) {
            listener.onFail(FilePickerResult.NO_PERMISSION)
        } else {
            if (!File(currPath).exists())
                currPath = context.getInternalStoragePath()

            mDialogView = LayoutInflater.from(context).inflate(R.layout.smtfp_directory_picker, null)
            updateItems()
            setupBreadcrumbs()

            // if a dialog's listview has height wrap_content, it calls getView way too often which can reduce performance
            // lets just measure it, then set a static height
            mDialogView.directory_picker_list.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val listener = this
                    val rect = Rect()
                    mDialogView.directory_picker_list.apply {
                        getGlobalVisibleRect(rect)
                        layoutParams.height = rect.bottom - rect.top
                        viewTreeObserver.removeOnGlobalLayoutListener(listener)
                    }
                }
            })

            val builder = AlertDialog.Builder(context)
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

    private fun getTitle() = context.resources.getString(if (pickFile) R.string.smtfp_select_file else R.string.smtfp_select_folder)

    private fun dialogDismissed() = listener.onFail(FilePickerResult.DISMISS)

    private fun updateItems() {
        var items = getItems(currPath)
        if (!containsDirectory(items) && !mFirstUpdate && !pickFile) {
            verifyPath()
            return
        }

        items = items.sortedWith(compareBy({ !it.isDirectory }, { it.name.toLowerCase() }))

        val adapter = ItemsAdapter(context, items) {
            if (it.isDirectory) {
                currPath = it.path
                updateItems()
            } else if (pickFile) {
                currPath = it.path
                verifyPath()
            }
        }

        mDialogView.apply {
            directory_picker_list.adapter = adapter
            directory_picker_list.addItemDecoration(RecyclerViewDivider(context))
            directory_picker_breadcrumbs.setBreadcrumb(currPath)
        }

        mFirstUpdate = false
    }

    private fun verifyPath() {
        val file = File(currPath)
        if ((pickFile && file.isFile) || (!pickFile && file.isDirectory)) {
            sendSuccess()
        }
    }

    private fun sendSuccess() {
        listener.onSuccess(currPath)
        mDialog.dismiss()
    }

    private fun setupBreadcrumbs() = mDialogView.directory_picker_breadcrumbs.setListener(this)

    private fun getItems(path: String): List<FileDirItem> {
        val items = ArrayList<FileDirItem>()
        val base = File(path)
        val files = base.listFiles() ?: return items
        for (file in files) {
            if (!showHidden && file.isHidden)
                continue

            val curPath = file.absolutePath
            val curName = curPath.getFilenameFromPath()
            val size = file.length()
            items.add(FileDirItem(curPath, curName, file.isDirectory, getChildren(file), size))
        }
        return items
    }

    private fun getChildren(file: File): Int {
        return if (file.listFiles() == null || !file.isDirectory)
            0
        else
            file.listFiles().filter { !it.isHidden || (it.isHidden && showHidden) }.size
    }

    private fun containsDirectory(items: List<FileDirItem>) = items.any { it.isDirectory }

    override fun breadcrumbClicked(id: Int) {
        if (id == 0) {
            StoragePickerDialog(context, currPath) {
                currPath = it
                updateItems()
            }
        } else {
            val item = mDialogView.directory_picker_breadcrumbs.getChildAt(id).tag as FileDirItem
            if (currPath != item.path.trimEnd('/')) {
                currPath = item.path
                updateItems()
            }
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
