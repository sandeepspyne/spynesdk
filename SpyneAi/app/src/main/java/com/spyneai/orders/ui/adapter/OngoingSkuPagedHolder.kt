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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.BaseApplication
import com.spyneai.R
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.activity.ShowRawImagesActivity
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity
import com.spyneai.threesixty.data.VideoLocalRepoV2
import com.spyneai.threesixty.ui.ThreeSixtyActivity
import com.spyneai.threesixty.ui.TrimActivity

class OngoingSkuPagedHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {
    val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
    val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    val tvImages: TextView = view.findViewById(R.id.tvImages)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    val cvMain: CardView = view.findViewById(R.id.cvMain)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            parent: ViewGroup
        ): OngoingSkuPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_draft_sku, parent, false)
            return OngoingSkuPagedHolder(context, view)
        }
    }

    fun bind(item: Sku?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Sku) {
        tvCategory.text = if (item.subcategoryName.isNullOrEmpty()) item.categoryName else item.subcategoryName
        tvDate.text = getFormattedDate(item.createdAt)

        if (item.thumbnail == null){
            if (item.categoryId == AppConstants.CARS_CATEGORY_ID && (item.categoryId == item.subcategoryId)) {
                Glide.with(context)
                    .load(R.drawable.three_sixty_thumbnail)
                    .into(ivThumbnail)
            } else if (Utilities.getPreference(context, AppConstants.CATEGORY_NAME)
                    .equals("Food & Beverages")
            ) {
                Glide.with(context)
                    .load(R.drawable.ic_food_thumbnail_draft)
                    .into(ivThumbnail)
            } else {
                Glide.with(context)
                    .load(R.mipmap.defaults)
                    .into(ivThumbnail)
            }
        }else {
            if (item.categoryId == AppConstants.CARS_CATEGORY_ID || item.categoryId == AppConstants.BIKES_CATEGORY_ID){
                Glide.with(context)
                    .load(item.thumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ivThumbnail)
            }else {
                context.loadSmartly(
                    item.thumbnail,
                    ivThumbnail
                )
            }
        }

        if (item.categoryId != AppConstants.CARS_CATEGORY_ID)
            try {
                if (Utilities.getPreference(context, AppConstants.CATEGORY_NAME).equals("Food & Beverages")) {
                    Glide.with(context)
                        .load(R.drawable.ic_food_thumbnail_draft)
                        .into(ivThumbnail)
                } else{
                    context.loadSmartly(item.thumbnail,
                        ivThumbnail)
                }
            }catch (e: Exception){

            }

        if (item.categoryId == AppConstants.CARS_CATEGORY_ID && (item.categoryId == item.subcategoryId)) {
            Glide.with(context)
                .load(R.drawable.three_sixty_thumbnail)
                .into(ivThumbnail)
        }

        tvSkuName.text = item.skuName
        tvImages.text = item.imagesCount.toString()

        cvMain.setOnClickListener {
            Intent(context, ShowRawImagesActivity::class.java)
                .apply {
                    putExtra(AppConstants.SKU_UUID,item.uuid)
                    putExtra(AppConstants.SKU_NAME,item.skuName)
                    putExtra(AppConstants.SKU_ID,item.skuId)
                    putExtra(AppConstants.PROJECT_ID,item.projectUuid)
                    putExtra(AppConstants.PROJECT_ID,item.projectId)
                    context.startActivity(this)
                }
        }
    }
}