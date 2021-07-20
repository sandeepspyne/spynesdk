package com.spyneai.orders.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel

class SkusAdapter(
    val context: Context,
    val getProjectList: List<GetProjectsResponse.Project_data>,
    val viewModel: MyOrdersViewModel,
    val skuList: ArrayList<GetProjectsResponse.Sku>
) : RecyclerView.Adapter<SkusAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvImages: TextView = view.findViewById(R.id.tvImages)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SkusAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_skus, parent, false)
        return SkusAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkusAdapter.ViewHolder, position: Int) {


        Glide.with(context) // replace with 'this' if it's in activity
            .load(getProjectList[position].sku[position].images[position].input_lres)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.ivThumbnail)

        val skuPosition = viewModel.position

        holder.tvSkuName.text = skuList[position].sku_name
        holder.tvImages.text = skuList[position].total_images.toString()


    }

    override fun getItemCount(): Int {
        return getProjectList.size
    }
}