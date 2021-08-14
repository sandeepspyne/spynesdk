package com.spyneai.draft.ui.adapter

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
import com.spyneai.draft.ui.DraftSkuDetailsActivity
import com.spyneai.draft.ui.DraftSkusActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.SkusAdapter
import com.spyneai.processedimages.ui.BikeImagesActivity
import com.spyneai.processedimages.ui.ShowImagesActivity
import com.spyneai.shoot.ui.base.ShootActivity

class DraftSkusAdapter (
    val context: Context,
    val projectId : String,
    val skuList: ArrayList<GetProjectsResponse.Sku>
) : RecyclerView.Adapter<DraftSkusAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvImages: TextView = view.findViewById(R.id.tvImages)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val cvMain: CardView = view.findViewById(R.id.cvMain)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_draft_sku, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCategory.text = skuList[position].category

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

           Intent(
                context,
                DraftSkuDetailsActivity::class.java
            ).apply {
                putExtra(AppConstants.FROM_DRAFTS, true)
               putExtra(AppConstants.PROJECT_ID, projectId)
                putExtra(AppConstants.CATEGORY_ID, "cat_d8R14zUNE")
                putExtra(AppConstants.SUB_CAT_ID,"prod_seY3vxhATCH")
                putExtra(AppConstants.SUB_CAT_NAME,skuList[position].category)
                putExtra(AppConstants.CATEGORY_NAME, "Automobiles")
                putExtra(AppConstants.SKU_NAME, skuList[position].sku_name)
                putExtra(AppConstants.SKU_CREATED, false)
                putExtra(AppConstants.SKU_ID, skuList[position].sku_id)
                putExtra(AppConstants.EXTERIOR_ANGLES, 8)
                putExtra("is_paid",skuList[position].paid)
                putExtra(AppConstants.IMAGE_TYPE,skuList[position].category)
                putExtra(AppConstants.IS_360,skuList[position].is360)
                context.startActivity(this)
            }

        }


    }

    override fun getItemCount(): Int {
        return skuList.size
    }
}