package com.spyneai.orders.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel

class MyCompletedProjectsAdapter(
    val context: Context,
    val getProjectList: List<GetProjectsResponse.Project_data>,
    val viewModel: MyOrdersViewModel
) : RecyclerView.Adapter<MyCompletedProjectsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvSkus: TextView = view.findViewById(R.id.tvSkus)
        val tvImages: TextView = view.findViewById(R.id.tvImages)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvPaid: TextView = view.findViewById(R.id.tvPaid)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val clMain: ConstraintLayout = view.findViewById(R.id.clMain)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCompletedProjectsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_projects, parent, false)
        return MyCompletedProjectsAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCompletedProjectsAdapter.ViewHolder, position: Int) {


        Glide.with(context) // replace with 'this' if it's in activity
            .load(getProjectList[position].sku[position].images[position].input_lres)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.ivThumbnail)

        holder.tvProjectName.text = getProjectList[position].project_name
        holder.tvSkus.text = getProjectList[position].total_sku.toString()
        holder.tvImages.text = getProjectList[position].sku[position].total_images.toString()

        holder.clMain.setOnClickListener {
            viewModel.position.value = position
        }


    }

    override fun getItemCount(): Int {
        return getProjectList.size
    }
}