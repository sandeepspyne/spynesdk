package com.spyneai.shoot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.shoot.data.model.ProjectDetailResponse

lateinit var projectChildAdapter: ProjectChildAdapter

class ProjectDetailAdapter(
    val context: Context, var projectList: ArrayList<ProjectDetailResponse.Sku>
) : RecyclerView.Adapter<ProjectDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val rvChildProject: RecyclerView = view.findViewById(R.id.rvChildProject)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_parent_projects, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.tvSkuName.text = projectList[position].sku_name

        projectChildAdapter = ProjectChildAdapter(
            context,
            projectList
        )

        viewHolder.rvChildProject.apply {
            this?.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            viewHolder.rvChildProject.setLayoutManager(layoutManager)
            this?.adapter = projectChildAdapter
        }

    }

    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount() = if (projectList == null) 0 else projectList.size


}