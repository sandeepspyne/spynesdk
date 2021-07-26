package com.spyneai.orders.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.processedimages.ui.BikeImagesActivity
import com.spyneai.processedimages.ui.ShowImagesActivity

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
        val tvPaid: TextView = view.findViewById(R.id.tvPaid)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val cvMain: CardView = view.findViewById(R.id.cvMain)

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

        if (skuList[position].paid.equals("true"))
            holder.tvPaid.visibility = View.VISIBLE


        try {
            Glide.with(context) // replace with 'this' if it's in activity
                .load(skuList[position].images[0].input_lres)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(holder.ivThumbnail)

        }catch (e: Exception){

        }

        holder.tvSkuName.text = skuList[position].sku_name
        holder.tvImages.text = skuList[position].total_images.toString()



        holder.cvMain.setOnClickListener {
            Utilities.savePrefrence(
                context,
                AppConstants.SKU_ID,
                skuList[position].sku_id
            )

            val intent = if (skuList[position].category == "cat_d8R14zUNx" || skuList[position].category == "Bikes")
                Intent(
                    context,
                    BikeImagesActivity::class.java
                ) else Intent(
                context,
                ShowImagesActivity::class.java
            )

            intent.putExtra(AppConstants.SKU_ID, skuList[position].sku_id)
            intent.putExtra("is_paid",skuList[position].paid)
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return skuList.size
    }
}