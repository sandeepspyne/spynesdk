package com.spyneai.draft.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.orders.data.paging.CompletedPagedHolder
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.sku.Sku

class ImagePagedAdapter(
    val context: Context,
    val categoryId: String,
    val status: String) : PagingDataAdapter<Image, RecyclerView.ViewHolder>(REPO_COMPARATOR) {

    object REPO_COMPARATOR : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(
            oldItem: Image,
            newItem: Image
        ) =
            oldItem.uuid == newItem.uuid

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: Image,
            newItem: Image
        ) = oldItem == newItem
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        getItem(position)?.let {
            when (status) {
                "draft" -> (holder as DraftImageHolder).bind(it)
                "ongoing" -> (holder as DraftImageHolder).bind(it)
                "completed" -> (holder as DraftImageHolder).bind(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (status) {
            "draft" -> DraftImageHolder.getInstance(context,categoryId,parent)
            "ongoing" -> DraftImageHolder.getInstance(context,categoryId,parent)
            else -> DraftImageHolder.getInstance(context,categoryId,parent)
        }
    }
}