package com.spyneai.shoot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.shoot.data.model.ProjectDetailResponse

class ProjectChildAdapter(
    val context: Context,
    var projectList: ArrayList<ProjectDetailResponse.Images>,
    val totalImages: Int
)
    : RecyclerView.Adapter<ProjectChildAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSkuImages: ImageView = view.findViewById(R.id.ivSkuImages)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_child_projects, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        try {
            Glide.with(context)
                .load(projectList[position].input_lres)
                .error(R.drawable.ic_uploading)
                .into(viewHolder.ivSkuImages)
        }catch (e: Exception){

        }
    }

    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount() = if (totalImages == null) 0 else totalImages


}