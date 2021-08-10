package com.spyneai.orders.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import com.spyneai.orders.ui.KarviShowImagesActivity
import com.spyneai.processedimages.ui.ShowImagesActivity

class SkusAdapter(
    val context: Context,
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
        val ivDownloadSKU: ImageView = view.findViewById(R.id.ivDownloadSKU)
        val cvMain: CardView = view.findViewById(R.id.cvMain)

        val flCategory: FrameLayout = view.findViewById(R.id.flCategory)
        val flImages: FrameLayout = view.findViewById(R.id.flImages)
        val tvCategories: TextView = view.findViewById(R.id.tvCategories)
        val tvImage: TextView = view.findViewById(R.id.tvImage)

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

        if (skuList[position].paid)
            holder.tvPaid.visibility = View.VISIBLE
        else
            holder.tvPaid.visibility = View.GONE

        try {
            Glide.with(context) // replace with 'this' if it's in activity
                .load(skuList[position].images[0].input_lres)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(holder.ivThumbnail)

        }catch (e: Exception){

        }

        if (context.getString(R.string.app_name) == "Sweep.ei"){
            holder.apply {
                tvCategory.visibility = View.INVISIBLE
                flCategory.visibility = View.INVISIBLE
                flImages.visibility = View.INVISIBLE
                tvCategories.visibility = View.INVISIBLE
                tvImage.visibility = View.INVISIBLE
            }
        }else{
            holder.tvCategory.text = skuList[position].category
            holder.tvImages.text = skuList[position].total_images.toString()
        }

        holder.tvSkuName.text = skuList[position].sku_name
        holder.tvDate.text = skuList[position].created_on


        if (context.getString(R.string.app_name) == "Karvi.com"){
            holder.ivDownloadSKU.visibility = View.INVISIBLE

            holder.cvMain.setOnClickListener {
                Utilities.savePrefrence(
                    context,
                    AppConstants.SKU_ID,
                    skuList[position].sku_id
                )
                var intent = Intent(context, KarviShowImagesActivity::class.java)
                intent.putExtra(AppConstants.SKU_ID, skuList[position].sku_id)
                intent.putExtra("is_paid",skuList[position].paid)
                intent.putExtra(AppConstants.IMAGE_TYPE,skuList[position].category)
                intent.putExtra(AppConstants.IS_360,skuList[position].is360)
                context.startActivity(intent)
            }
        }else{
            holder.cvMain.setOnClickListener {
                Utilities.savePrefrence(
                    context,
                    AppConstants.SKU_ID,
                    skuList[position].sku_id
                )

                val intent = Intent(
                    context,
                    ShowImagesActivity::class.java
                )
                intent.putExtra(AppConstants.SKU_ID, skuList[position].sku_id)
                intent.putExtra("is_paid",skuList[position].paid)
                intent.putExtra(AppConstants.IMAGE_TYPE,skuList[position].category)
                intent.putExtra(AppConstants.IS_360,skuList[position].is360)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return skuList.size
    }
}