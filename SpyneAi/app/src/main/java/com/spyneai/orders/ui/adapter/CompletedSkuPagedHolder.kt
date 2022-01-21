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
import com.spyneai.draft.ui.DraftSkuDetailsActivity
import com.spyneai.draft.ui.adapter.DraftSkuPagedHolder
import com.spyneai.getFormattedDate
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.processedimages.ui.ProcessedImageActivity
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.threesixty.data.VideoLocalRepository
import com.spyneai.threesixty.ui.ThreeSixtyActivity
import com.spyneai.threesixty.ui.TrimActivity

class CompletedSkuPagedHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {
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

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            parent: ViewGroup
        ): CompletedSkuPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_completed_skus, parent, false)
            return CompletedSkuPagedHolder(context, view)
        }
    }

    fun bind(item: Sku?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Sku) {
        if (item.isPaid)
            tvPaid.visibility = View.VISIBLE
        else
            tvPaid.visibility = View.GONE

        tvCategory.text = if (item.subcategoryName.isNullOrEmpty()) item.categoryName else item.subcategoryName

        try {
            Glide.with(context) // replace with 'this' if it's in activity
                .load(item.thumbnail)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(ivThumbnail)

        } catch (e: Exception) {

        }

        if (context.getString(R.string.app_name) == AppConstants.SWEEP) {
            apply {
                tvCategory.visibility = View.INVISIBLE
                flCategory.visibility = View.INVISIBLE
                flImages.visibility = View.INVISIBLE
                tvCategories.visibility = View.INVISIBLE
                tvImage.visibility = View.INVISIBLE
            }
        } else {
            tvCategory.text = item.categoryName
            tvImages.text = item.imagesCount.toString()
        }

        tvSkuName.text = item.skuName
        tvDate.text = getFormattedDate(item.createdAt)

        ivDownloadSKU.setOnClickListener {
            //delete sku images
        }

        if (context.getString(R.string.app_name) == AppConstants.KARVI)
            ivDownloadSKU.visibility = View.INVISIBLE

        cvMain.setOnClickListener {
            Utilities.savePrefrence(
                context,
                AppConstants.SKU_ID,
                item.skuId
            )

            val intent = Intent(
                context,
                ProcessedImageActivity::class.java
            )
            intent.putExtra(AppConstants.PROJECT_ID, item.projectId)
            intent.putExtra(AppConstants.PROJECT_UUIID, item.projectUuid)
            intent.putExtra(AppConstants.SKU_ID, item.skuId)
            intent.putExtra(AppConstants.SKU_UUID, item.uuid)
            intent.putExtra(AppConstants.SKU_NAME, item.skuName)
            intent.putExtra(AppConstants.CATEGORY_ID, item.categoryId)
            intent.putExtra(AppConstants.CATEGORY_NAME, item.categoryName)
            intent.putExtra(AppConstants.SUB_CAT_ID, item.subcategoryId)
            intent.putExtra(AppConstants.EXTERIOR_ANGLES, item.initialFrames)
            intent.putExtra("is_paid",item.isPaid)
            intent.putExtra(AppConstants.IMAGE_TYPE,item.categoryName)
            intent.putExtra(AppConstants.IS_360,item.isThreeSixty)
            context.startActivity(intent)
        }
    }

}