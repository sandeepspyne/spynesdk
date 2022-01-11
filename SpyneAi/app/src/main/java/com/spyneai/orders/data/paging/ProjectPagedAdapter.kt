package com.spyneai.orders.data.paging

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil

class ProjectPagedAdapter : PagingDataAdapter<ProjectPagedRes.ProjectPagedResItem, ProjectPagedHolder>(REPO_COMPARATOR) {
// body is unchanged


    object REPO_COMPARATOR : DiffUtil.ItemCallback<ProjectPagedRes.ProjectPagedResItem>() {
        override fun areItemsTheSame(oldItem: ProjectPagedRes.ProjectPagedResItem, newItem: ProjectPagedRes.ProjectPagedResItem) =
            oldItem.prodCatId == newItem.prodCatId

        override fun areContentsTheSame(oldItem: ProjectPagedRes.ProjectPagedResItem, newItem: ProjectPagedRes.ProjectPagedResItem) =
            oldItem == newItem
    }

    override fun onBindViewHolder(holder: ProjectPagedHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectPagedHolder {
        return ProjectPagedHolder.getInstance(parent)
    }
}