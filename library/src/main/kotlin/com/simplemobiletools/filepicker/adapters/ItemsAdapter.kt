package com.simplemobiletools.filepicker.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.simplemobiletools.filepicker.R
import com.simplemobiletools.filepicker.extensions.formatSize
import com.simplemobiletools.filepicker.extensions.isGif
import com.simplemobiletools.filepicker.models.FileDirItem
import kotlinx.android.synthetic.main.smtfp_list_item.view.*
import java.io.File

class ItemsAdapter(val context: Context, private val mItems: List<FileDirItem>, val itemClick: (FileDirItem) -> Unit) :
        RecyclerView.Adapter<ItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.smtfp_list_item, parent, false)
        return MyViewHolder(context, view, itemClick)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindView(mItems[position])
    }

    override fun getItemCount() = mItems.size

    class MyViewHolder(val context: Context, view: View, val itemClick: (FileDirItem) -> (Unit)) : RecyclerView.ViewHolder(view) {
        fun bindView(fileDirItem: FileDirItem) {
            itemView.item_name.text = fileDirItem.name

            if (fileDirItem.isDirectory) {
                Glide.with(context).load(R.mipmap.smtfp_directory).diskCacheStrategy(getCacheStrategy(fileDirItem)).centerCrop().crossFade().into(itemView.item_icon)
                itemView.item_details.text = getChildrenCnt(fileDirItem)
            } else {
                Glide.with(context).load(fileDirItem.path).diskCacheStrategy(getCacheStrategy(fileDirItem)).error(R.mipmap.smtfp_file).centerCrop().crossFade().into(itemView.item_icon)
                itemView.item_details.text = fileDirItem.size.formatSize()
            }

            itemView.setOnClickListener { itemClick(fileDirItem) }
        }

        private fun getCacheStrategy(item: FileDirItem) = if (File(item.path).isGif()) DiskCacheStrategy.NONE else DiskCacheStrategy.RESULT

        private fun getChildrenCnt(item: FileDirItem): String {
            val children = item.children
            return context.resources.getQuantityString(R.plurals.smtfp_items, children, children)
        }
    }
}
