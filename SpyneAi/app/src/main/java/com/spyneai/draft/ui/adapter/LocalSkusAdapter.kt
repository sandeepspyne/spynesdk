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
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.threesixty.data.VideoLocalRepository
import com.spyneai.threesixty.ui.ThreeSixtyActivity
import com.spyneai.threesixty.ui.TrimActivity
import com.spyneai.toDate

class LocalSkusAdapter(
    val context: Context,
    val skuList: ArrayList<Sku>
) : RecyclerView.Adapter<LocalSkusAdapter.ViewHolder>() {

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
        if (skuList[position].subcategoryName == "" || skuList[position].subcategoryName == null)
            holder.tvCategory.text = skuList[position].categoryName
        else
            holder.tvCategory.text = skuList[position].subcategoryName

        if (skuList[position].categoryId == AppConstants.CARS_CATEGORY_ID && (skuList[position].categoryId == skuList[position].subcategoryId))
            holder.tvCategory.text = "Automobiles"

        try {

            if (skuList[position].thumbnail != null) {
                Glide.with(context) // replace with 'this' if it's in activity
                    .load(skuList[position].thumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                    .into(holder.ivThumbnail)
            } else {
                if (Utilities.getPreference(context, AppConstants.CATEGORY_NAME)
                        .equals("Food & Beverages")
                ) {
                    Glide.with(context)
                        .load(R.drawable.ic_food_thumbnail_draft)
                        .into(holder.ivThumbnail)
                } else {
                    Glide.with(context) // replace with 'this' if it's in activity
                        .load(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(holder.ivThumbnail)
                }
            }
        } catch (e: Exception) {

        }

        if (skuList[position].categoryId == AppConstants.CARS_CATEGORY_ID && (skuList[position].categoryId == skuList[position].subcategoryId)) {
            Glide.with(context)
                .load(R.drawable.three_sixty_thumbnail)
                .into(holder.ivThumbnail)
        }

        holder.tvSkuName.text = skuList[position].skuName
        holder.tvImages.text = skuList[position].totalImages.toString()
        holder.tvDate.text = skuList[position].createdOn?.toDate()

        holder.cvMain.setOnClickListener {
            Utilities.savePrefrence(
                context,
                AppConstants.SKU_ID,
                skuList[position].skuId
            )

            if (skuList[position].categoryId == skuList[position].subcategoryId) {
                val videoPath = VideoLocalRepository().getVideoPath(skuList[position].skuId!!)

                val intent = when {
                    VideoLocalRepository().getVideoId(skuList[position].skuId!!) != null -> {
                        Intent(context, ShootActivity::class.java)
                            .apply {
                                putExtra(AppConstants.FROM_VIDEO,true)
                                putExtra(AppConstants.TOTAL_FRAME,skuList[position].threeSixtyFrames)
                            }
                    }
                    videoPath != null && videoPath != "" -> {
                        Intent(context, TrimActivity::class.java)
                    }
                    else -> {
                        Intent(context, ThreeSixtyActivity::class.java)
                    }
                }

                intent.apply {
                    putExtra(AppConstants.FROM_VIDEO, true)
                    putExtra(AppConstants.FROM_LOCAL_DB, true)
                    putExtra(AppConstants.FROM_DRAFTS, true)
                    putExtra(AppConstants.CATEGORY_ID, skuList[position].categoryId)
                    putExtra(AppConstants.PROJECT_ID, skuList[position].projectId)
                    putExtra(AppConstants.SUB_CAT_ID, skuList[position].subcategoryId)
                    putExtra(AppConstants.SUB_CAT_NAME, skuList[position].subcategoryName)
                    putExtra(AppConstants.CATEGORY_NAME, skuList[position].categoryName)
                    putExtra(AppConstants.SKU_NAME, skuList[position].skuName)
                    putExtra(AppConstants.PROJECT_NAME, skuList[position].skuName)
                    putExtra(AppConstants.SKU_COUNT, skuList.size)
                    putExtra(AppConstants.SKU_CREATED, false)
                    putExtra(AppConstants.SKU_ID, skuList[position].skuId)
                    putExtra(AppConstants.EXTERIOR_ANGLES, skuList[position].exteriorAngles)
                    //putExtra("is_paid",skuList[position].paid)
                    //putExtra(AppConstants.IMAGE_TYPE,skuList[position].category)
                    putExtra(AppConstants.IS_360, skuList[position].is360)
                    putExtra(AppConstants.VIDEO_PATH, videoPath)
                }

                context.startActivity(intent)
            } else {
                val draftIntent = Intent(
                    context,
                    DraftSkuDetailsActivity::class.java
                ).apply {
                    putExtra(AppConstants.FROM_LOCAL_DB, true)
                    putExtra(AppConstants.FROM_DRAFTS, true)
                    putExtra(AppConstants.PROJECT_ID, skuList[position].projectId)
                    putExtra(AppConstants.CATEGORY_ID, skuList[position].categoryId)
                    putExtra(AppConstants.SUB_CAT_ID, skuList[position].subcategoryId)
                    putExtra(AppConstants.SUB_CAT_NAME, skuList[position].subcategoryName)
                    putExtra(AppConstants.CATEGORY_NAME, skuList[position].categoryName)
                    putExtra(AppConstants.SKU_NAME, skuList[position].skuName)
                    putExtra(AppConstants.PROJECT_NAME, skuList[position].skuName)
                    putExtra(AppConstants.SKU_COUNT, skuList.size)
                    putExtra(AppConstants.SKU_CREATED, false)
                    putExtra(AppConstants.SKU_ID, skuList[position].skuId)
                    putExtra(AppConstants.EXTERIOR_ANGLES, skuList[position].exteriorAngles)
                    //putExtra("is_paid",skuList[position].paid)
                    //putExtra(AppConstants.IMAGE_TYPE,skuList[position].category)
                    putExtra(AppConstants.IS_360, skuList[position].is360)
                }

                draftIntent.putExtra(
                    AppConstants.EXTERIOR_ANGLES,
                    skuList[position].exteriorAngles
                )

                val s = ""

//                when (skuList[position].subcategoryId) {
//                    "prod_4CW50lj2sNMCS" -> draftIntent.putExtra(AppConstants.EXTERIOR_ANGLES, 5)
//                    "prod_4CW50lj2sNMF" -> draftIntent.putExtra(AppConstants.EXTERIOR_ANGLES, 6)
//                    else -> {
//
//                    }
//
//                }

                if (skuList[position].threeSixtyFrames != null && skuList[position].threeSixtyFrames != 0){
                    draftIntent.apply {
                        putExtra(AppConstants.FROM_VIDEO, true)
                        putExtra(AppConstants.TOTAL_FRAME, skuList[position].threeSixtyFrames)
                    }
                }

                context.startActivity(draftIntent)
            }

        }


    }

    override fun getItemCount(): Int {
        return skuList.size
    }
}