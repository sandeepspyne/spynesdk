package com.spyneai.orders.ui.adapter

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.ShowImagesActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetCompletedSKUsResponse
import kotlinx.android.synthetic.main.activity_generate_gif.*

class MyCompletedOrdersAdapter(
    val context: Context,
    val completedSkuList: ArrayList<GetCompletedSKUsResponse.Data>
) : RecyclerView.Adapter<MyCompletedOrdersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_images_count: TextView = view.findViewById(R.id.tv_images_count)
        val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
        val tvSubCat: TextView = view.findViewById(R.id.tvSubCat)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val tvSource: TextView = view.findViewById(R.id.tvSource)
        val llPaid: LinearLayout = view.findViewById(R.id.llPaid)
        val iv_thumbnail_completed: ImageView = view.findViewById(R.id.iv_thumbnail_completed)
        val ivDownloadSKU: ImageView = view.findViewById(R.id.ivDownloadSKU)
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

        if (completedSkuList[position].paid)
            holder.llPaid.visibility = View.VISIBLE

        holder.tv_images_count.text = completedSkuList[position].total_images.toString()
        holder.tvSubCat.text = completedSkuList[position].sub_category
        holder.tvDate.text = completedSkuList[position].created_date
        holder.tvCategoryName.text = completedSkuList[position].category
        holder.tvSkuName.text = completedSkuList[position].sku_name
        holder.tvProjectName.text = completedSkuList[position].project_name
        holder.tvSource.text = completedSkuList[position].source

        Glide.with(context) // replace with 'this' if it's in activity
            .load(completedSkuList[position].thumbnail)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.iv_thumbnail_completed)

        holder.ivDownloadSKU.setOnClickListener {
            Utilities.savePrefrence(
                context,
                AppConstants.SKU_ID,
                completedSkuList[position].sku_id
            )
            val intent = Intent(
                context,
                ShowImagesActivity::class.java
            )
            context.startActivity(intent)
        }



//        holder.cvDownload.setOnClickListener { view ->
//            view.findNavController().navigate(R.id.nav_request_download)
//        }
    }

    override fun getItemCount(): Int {
        return completedSkuList.size
    }
}