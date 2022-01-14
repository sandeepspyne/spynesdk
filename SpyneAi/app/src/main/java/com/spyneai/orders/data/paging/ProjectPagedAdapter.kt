package com.spyneai.orders.data.paging

import android.content.Context
import android.view.ViewGroup
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.shoot.repository.model.project.Project

@ExperimentalPagingApi
class ProjectPagedAdapter(
    val context : Context,
    val status: String) :
    PagingDataAdapter<Project, RecyclerView.ViewHolder>(REPO_COMPARATOR) {

    object REPO_COMPARATOR : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(
            oldItem: Project,
            newItem: Project
        ) =
            oldItem.projectId == newItem.projectId

        override fun areContentsTheSame(
            oldItem: Project,
            newItem: Project
        ) =
            oldItem == newItem
    }

    @ExperimentalPagingApi
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
            "draft" -> DraftPagedHolder.getInstance(context,parent)
            "ongoing" -> OngoingPagedHolder.getInstance(context,parent)
            else -> CompletedPagedHolder.getInstance(context,parent)
        }
    }
}