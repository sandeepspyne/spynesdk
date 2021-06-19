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
import com.spyneai.orders.data.response.CompletedSKUsResponse

class MyCompletedOrdersAdapter(
    val context: Context,
    val completedSkuList: ArrayList<CompletedSKUsResponse.Data>
) : RecyclerView.Adapter<MyCompletedOrdersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_images_count: TextView = view.findViewById(R.id.tv_images_count)
        val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
        val tvSubCat: TextView = view.findViewById(R.id.tvSubCat)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val tvSource: TextView = view.findViewById(R.id.tvSource)
        val iv_thumbnail_completed: ImageView = view.findViewById(R.id.iv_thumbnail_completed)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCompletedOrdersAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_completed_orders, parent, false)

        return MyCompletedOrdersAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCompletedOrdersAdapter.ViewHolder, position: Int) {

        holder.tv_images_count.text = completedSkuList[position].total_images.toString()
        holder.tvSubCat.text = completedSkuList[position].sub_category
        holder.tvDate.text = completedSkuList[position].created_date
        holder.tvCategoryName.text = completedSkuList[position].category
        holder.tvSkuName.text = completedSkuList[position].sku_name

        Glide.with(context) // replace with 'this' if it's in activity
            .load(completedSkuList[position].thumbnail)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.iv_thumbnail_completed)



//        holder.cvDownload.setOnClickListener { view ->
//            view.findNavController().navigate(R.id.nav_request_download)
//        }
    }

    override fun getItemCount(): Int {
        return 10
    }
}