package com.spyneai.orders.ui.adapter

import android.content.Context
import android.util.Log
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

    override fun getItemViewType(position: Int): Int {
        if (position == ongoingSkuList.size - 1){
            return 1
        }
        return 0
    }

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
        val tvImageCount: TextView = view.findViewById(R.id.tvImageCount)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ):ViewHolder {
        val view : View?

        if (viewType == 0){
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_my_ongoing_orders, parent, false)
        }else{
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_last_ongoing_order, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dots = "..."

        holder.tv_images_count_ongoing.text = ongoingSkuList[position].total_images.toString()
        holder.tvSubCategory.text = ongoingSkuList[position].sub_category
        if (ongoingSkuList[position].sub_category == "null")
            holder.tvSubCategory.text = "prod_Gcg69Rkxa"

        holder.tvDateOngoing.text = ongoingSkuList[position].created_date
        holder.tvCategoryName.text = ongoingSkuList[position].category
        holder.tvSkuName.text = ongoingSkuList[position].sku_name + dots
        holder.tvSkuId.text = ongoingSkuList[position].sku_id + dots
        holder.tvProjectId.text = ongoingSkuList[position].project_id + dots
        holder.tvImageCount.text = ongoingSkuList[position].total_processed +"/" +ongoingSkuList[position].total_images

        Glide.with(context) // replace with 'this' if it's in activity
            .load(ongoingSkuList[position].thumbnail)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.iv_thumbnail_ongoing)

    }

    override fun getItemCount(): Int {
        return ongoingSkuList.size
    }
}