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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.draft.ui.DraftSkuDetailsActivity
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity
import com.spyneai.threesixty.data.VideoLocalRepository
import com.spyneai.threesixty.ui.ThreeSixtyActivity
import com.spyneai.threesixty.ui.TrimActivity

class DraftSkuPagedHolder(
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
        ): DraftSkuPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_draft_sku, parent, false)
            return DraftSkuPagedHolder(context, view)
        }
    }

    fun bind(item: Sku?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Sku) {
        tvCategory.text = item.subcategoryName
        try {
            tvDate.text = getFormattedDate(item.createdOn)
        }catch (e : java.lang.Exception){

        }


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
            Utilities.savePrefrence(
                context,
                AppConstants.SKU_ID,
                item.skuId
            )

            if (item.categoryId == AppConstants.CARS_CATEGORY_ID && (item.categoryId == item.subcategoryId)) {
                val videoPath = VideoLocalRepository().getVideoPath(item.uuid)

                val intent = when{
                    item.videoId != null -> {
                        Intent(context, ShootActivity::class.java)
                    }
                    videoPath != null && videoPath != "" -> {
                        Intent(context, TrimActivity::class.java)
                    }
                    else -> {
                        Intent(context, ThreeSixtyActivity::class.java)
                    }
                }

                intent.apply {
                    putExtra(AppConstants.FROM_LOCAL_DB, true)
                    putExtra(AppConstants.FROM_DRAFTS, true)
                    putExtra(AppConstants.FROM_VIDEO, true)
                    putExtra(AppConstants.PROJECT_ID, item.projectId)
                    putExtra(AppConstants.PROJECT_UUIID, item.projectUuid)
                    putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                    putExtra(AppConstants.SUB_CAT_ID,item.subcategoryId)
                    putExtra(AppConstants.SUB_CAT_NAME,item.subcategoryName)
                    putExtra(AppConstants.CATEGORY_NAME, item.categoryName)
                    putExtra(AppConstants.SKU_NAME, item.skuName)
                    putExtra(AppConstants.PROJECT_NAME, item.skuName)
                   // putExtra(AppConstants.SKU_COUNT, item.imagesCount)
                    putExtra(AppConstants.SKU_CREATED, false)
                    putExtra(AppConstants.SKU_ID, item.skuId)
                    putExtra(AppConstants.SKU_UUID, item.uuid)
                    putExtra(AppConstants.EXTERIOR_ANGLES, item.initialFrames)
                    //putExtra("is_paid",item.paid)
                    //putExtra(AppConstants.IMAGE_TYPE,item.category)
                    putExtra(AppConstants.IS_360,item.isThreeSixty)
                    putExtra(AppConstants.VIDEO_PATH,videoPath)
                }

                context.startActivity(intent)
            }else {
                val intent = if (item.imagesCount > 0) {
                    Intent(
                        context,
                        DraftSkuDetailsActivity::class.java
                    )
                }else {
                    when (item.categoryId) {
                        AppConstants.CARS_CATEGORY_ID, AppConstants.BIKES_CATEGORY_ID -> {
                           Intent(
                                context,
                                ShootActivity::class.java
                            )
                        }
                        else -> Intent(
                            context,
                            ShootPortraitActivity::class.java
                        )
                    }
                }

                intent.apply {
                    putExtra(AppConstants.FROM_LOCAL_DB, true)
                    putExtra(AppConstants.FROM_DRAFTS, true)
                    putExtra(AppConstants.PROJECT_ID,item.projectId)
                    putExtra(AppConstants.PROJECT_UUIID,item.projectUuid)
                    putExtra(AppConstants.CATEGORY_NAME, item.categoryName)
                    putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                    putExtra(AppConstants.SUB_CAT_ID,item.subcategoryId)
                    putExtra(AppConstants.SUB_CAT_NAME,item.subcategoryName)
                    putExtra(AppConstants.SKU_NAME, item.skuName)
                    putExtra(AppConstants.PROJECT_NAME, item.skuName)
                   // putExtra(AppConstants.SKU_COUNT, skuList.size)
                    putExtra(AppConstants.SKU_CREATED, true)
                    putExtra(AppConstants.SKU_ID, item.skuId)
                    putExtra(AppConstants.SKU_UUID, item.uuid)
                    putExtra(AppConstants.EXTERIOR_ANGLES, item.initialFrames)
                    putExtra("is_paid",item.isPaid)
                    //putExtra(AppConstants.IMAGE_TYPE,item.category)
                    putExtra(AppConstants.IS_360,item.isThreeSixty)
                    putExtra(AppConstants.RESUME_EXTERIOR, true)
                    putExtra(AppConstants.RESUME_INTERIOR, false)
                    putExtra(AppConstants.RESUME_MISC, false)
                }

                if (!item.videoId.isNullOrEmpty()){
                    intent.apply {
                        putExtra(AppConstants.FROM_VIDEO, true)
                        putExtra(AppConstants.TOTAL_FRAME, item.imagesCount)
                    }
                }

                context.startActivity(intent)
            }
        }
    }
}