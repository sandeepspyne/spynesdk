package com.spyneai.orders.data.paging

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.draft.ui.DraftPagedSkuActivity
import com.spyneai.draft.ui.DraftSkusActivity
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity

class DraftPagedHolder(
    val view: View,
    val context: Context
) : RecyclerView.ViewHolder(view) {

    val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
    val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    val tvSkus: TextView = view.findViewById(R.id.tvSkus)
    val llThreeSixty: LinearLayout = view.findViewById(R.id.llThreeSixty)
    val tvImages: TextView = view.findViewById(R.id.tvImages)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val tvPaid: TextView = view.findViewById(R.id.tvPaid)
    val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    val cvMain: CardView = view.findViewById(R.id.cvMain)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(context: Context, parent: ViewGroup): DraftPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_draft_project, parent, false)
            return DraftPagedHolder(view, context)
        }
    }

    @ExperimentalPagingApi
    fun bind(item: Project?) {
        item?.let {
            showData(item)
        }
    }

    @ExperimentalPagingApi
    private fun showData(item: Project) {
        if (item.categoryId == AppConstants.CARS_CATEGORY_ID && (item.categoryId == item.subCategoryId)) {
            llThreeSixty.visibility = View.VISIBLE
            tvCategory.text = "Automobiles"
        } else {
            tvCategory.text = item.categoryName
            llThreeSixty.visibility = View.GONE
        }

        try {
            if (item.imagesCount == 0) {
                if (item.categoryId == AppConstants.CARS_CATEGORY_ID && (item.categoryId == item.subCategoryId)) {
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
            } else {
                if (item.thumbnail == null) {
                    if (item.categoryId == AppConstants.CARS_CATEGORY_ID && (item.categoryId == item.subCategoryId)) {
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
                } else {
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }

        tvProjectName.text = item.projectName
        tvSkus.text = item.skuCount.toString()
        try {
            tvDate.text = getFormattedDate(item.createdOn)
        }catch (e : java.lang.Exception){
            val s = ""
        }
        tvImages.text = item.imagesCount.toString()

        cvMain.setOnClickListener {
            if (item.categoryId == item.subCategoryId) {
                context.startActivity(getDraftIntent(item))
            } else {
                when {
                    item.skuCount == 0 -> {
                        Intent(context, ShootActivity::class.java)
                            .apply {
                                putExtra(AppConstants.FROM_DRAFTS, true)
                                putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                                putExtra(AppConstants.CATEGORY_NAME, item.categoryName)
                                putExtra(AppConstants.PROJECT_ID, item.projectId)
                                putExtra(AppConstants.SKU_NAME, item.projectName)
                                putExtra(AppConstants.SKU_CREATED, false)
                                context.startActivity(this)
                            }
                    }

                    item.categoryName == "Footwear" && item.subCategoryName == "" -> {
                        Utilities.savePrefrence(
                            context,
                            AppConstants.CATEGORY_NAME,
                            item.categoryName
                        )

                        Intent(context, ShootPortraitActivity::class.java)
                            .apply {
                                putExtra(AppConstants.FROM_DRAFTS, true)
                                putExtra(AppConstants.CATEGORY_NAME, item.categoryName)
                                putExtra(AppConstants.CATEGORY_ID, item.categoryId)
                                putExtra(AppConstants.PROJECT_ID, item.projectId)
                                putExtra(AppConstants.SKU_NAME, item.projectName)
                                putExtra(AppConstants.SUB_CAT_NAME, item.subCategoryId)
                                putExtra(AppConstants.SUB_CAT_ID, item.subCategoryId)
                                putExtra(AppConstants.SKU_CREATED, true)
                                putExtra(AppConstants.FROM_DRAFTS, true)
                                //putExtra(AppConstants.SKU_ID, item.sku[0].sku_id)
                                context.startActivity(this)
                            }

                    }

                    else -> {
                        context.startActivity(getDraftIntent(item))
                    }
                }
            }
        }
    }

    @ExperimentalPagingApi
    fun getDraftIntent(item: Project) = Intent(context, DraftPagedSkuActivity::class.java).apply {
        putExtra("position", position)
        putExtra(AppConstants.FROM_LOCAL_DB, true)
        putExtra(AppConstants.PROJECT_NAME, item.projectName)
        putExtra(AppConstants.SKU_COUNT, item.skuCount)
        putExtra(AppConstants.PROJECT_UUIID, item.uuid)
        putExtra(AppConstants.PROJECT_ID, item.projectId)
    }
}