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
import com.spyneai.orders.data.response.GetOngoingSkusResponse

class MyOngoingOrdersAdapter(
    val context: Context,
    val ongoingSkuList: ArrayList<GetOngoingSkusResponse.Data>
) : RecyclerView.Adapter<MyOngoingOrdersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tv_images_count_ongoing: TextView = view.findViewById(R.id.tv_images_count_ongoing)
        val tvProjectId: TextView = view.findViewById(R.id.tvProjectId)
        val tvSubCategory: TextView = view.findViewById(R.id.tvSubCategory)
        val tvDateOngoing: TextView = view.findViewById(R.id.tvDateOngoing)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val tvSkuId: TextView = view.findViewById(R.id.tvSkuId)
        val tvNoOfSku: TextView = view.findViewById(R.id.tvNoOfSku)
        val iv_thumbnail_ongoing: ImageView = view.findViewById(R.id.iv_thumbnail_ongoing)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyOngoingOrdersAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_ongoing_orders, parent, false)
        return MyOngoingOrdersAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyOngoingOrdersAdapter.ViewHolder, position: Int) {

        holder.tv_images_count_ongoing.text = ongoingSkuList[position].total_images.toString()
        holder.tvSubCategory.text = ongoingSkuList[position].sub_category
        holder.tvDateOngoing.text = ongoingSkuList[position].created_date
        holder.tvCategoryName.text = ongoingSkuList[position].category
        holder.tvSkuName.text = ongoingSkuList[position].sku_name
        holder.tvSkuId.text = ongoingSkuList[position].sku_id

        Glide.with(context) // replace with 'this' if it's in activity
            .load(ongoingSkuList[position].thumbnail)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.iv_thumbnail_ongoing)

    }

    override fun getItemCount(): Int {
        return 5
    }
}