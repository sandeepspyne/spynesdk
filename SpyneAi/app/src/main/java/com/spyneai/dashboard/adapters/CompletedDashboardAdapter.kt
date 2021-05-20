package com.spyneai.dashboard.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.adapter.CompletedProjectAdapter
import com.spyneai.model.projects.CompletedProjectResponse

class CompletedDashboardAdapter (
    val context: Context,
    val completedProjectList: ArrayList<CompletedProjectResponse>)
    : RecyclerView.Adapter<CompletedDashboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvSku: TextView = view.findViewById(R.id.tvSku)
        val tvDate: TextView = view.findViewById(R.id.tvDate)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_completed_dashboard, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvSku.text = completedProjectList[position].sku_name
        holder.tvDate.text = completedProjectList[position].created_at
        Glide.with(context)
            .load(completedProjectList[position].output_image_url)
            .into(holder.ivImage)

    }

    override fun getItemCount() = completedProjectList.size


}