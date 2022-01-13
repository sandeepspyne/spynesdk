package com.spyneai.orders.data.paging

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ProjectPagedAdapter(
    val context : Context,
    val status: String) :
    PagingDataAdapter<ProjectPagedRes.ProjectPagedResItem, RecyclerView.ViewHolder>(REPO_COMPARATOR) {
// body is unchanged

    object REPO_COMPARATOR : DiffUtil.ItemCallback<ProjectPagedRes.ProjectPagedResItem>() {
        override fun areItemsTheSame(
            oldItem: ProjectPagedRes.ProjectPagedResItem,
            newItem: ProjectPagedRes.ProjectPagedResItem
        ) =
            oldItem.prodCatId == newItem.prodCatId

        override fun areContentsTheSame(
            oldItem: ProjectPagedRes.ProjectPagedResItem,
            newItem: ProjectPagedRes.ProjectPagedResItem
        ) =
            oldItem == newItem
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        getItem(position)?.let {
            when (status) {
                "draft" -> (holder as DraftPagedHolder).bind(it)
                "ongoing" -> (holder as OngoingPagedHolder).bind(it)
                "completed" -> (holder as CompletedPagedHolder).bind(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (status) {
            "draft" -> DraftPagedHolder.getInstance(parent)
            "ongoing" -> OngoingPagedHolder.getInstance(context,parent)
            else -> CompletedPagedHolder.getInstance(parent)
        }
    }
}