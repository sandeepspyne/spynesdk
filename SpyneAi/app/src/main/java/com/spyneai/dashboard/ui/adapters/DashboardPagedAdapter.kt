package com.spyneai.dashboard.ui.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.orders.data.paging.CompletedPagedHolder
import com.spyneai.orders.data.paging.DraftPagedHolder
import com.spyneai.orders.data.paging.OngoingPagedHolder
import com.spyneai.shoot.repository.model.project.Project

@ExperimentalPagingApi
class DashboardPagedAdapter(
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
                "ongoing" -> (holder as DashboardOngoingPagedHolder).bind(it)
                "completed" -> (holder as DashboardCompletedHolder).bind(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (status) {
            "ongoing" -> DashboardOngoingPagedHolder.getInstance(context,parent)
            else -> DashboardCompletedHolder.getInstance(context,parent)
        }
    }
}