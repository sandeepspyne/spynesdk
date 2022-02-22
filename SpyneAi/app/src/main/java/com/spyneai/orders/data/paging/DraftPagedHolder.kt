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
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.activity.SkuPagedActivity
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
        tvCategory.text = item.categoryName
        llThreeSixty.visibility = View.GONE

        try {
            if (item.thumbnail == null) {
                Glide.with(context)
                    .load(R.drawable.app_logo)
                    .into(ivThumbnail)
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
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }

        tvProjectName.text = item.projectName
        tvSkus.text = item.skuCount.toString()
        tvDate.text = getFormattedDate(item.createdAt)

        tvImages.text = item.imagesCount.toString()

        cvMain.setOnClickListener {
            context.startActivity(getDraftIntent(item))
        }
    }

    @ExperimentalPagingApi
    fun getDraftIntent(item: Project) = Intent(context, SkuPagedActivity::class.java).apply {
        putExtra(AppConstants.STATUS, "draft")
        putExtra("position", position)
        putExtra(AppConstants.FROM_LOCAL_DB, true)
        putExtra(AppConstants.PROJECT_NAME, item.projectName)
        putExtra(AppConstants.SKU_COUNT, item.skuCount)
        putExtra(AppConstants.PROJECT_UUIID, item.uuid)
        putExtra(AppConstants.PROJECT_ID, item.projectId)
    }
}