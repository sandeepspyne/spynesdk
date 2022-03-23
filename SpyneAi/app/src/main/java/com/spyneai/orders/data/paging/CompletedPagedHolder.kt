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
import com.spyneai.R
import com.spyneai.getFormattedDate
import com.spyneai.getTimeStamp
import com.spyneai.needs.AppConstants
import com.spyneai.orders.ui.activity.SkuPagedActivity
import com.spyneai.shoot.repository.model.project.Project

@ExperimentalPagingApi
class CompletedPagedHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {

    val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
    val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    val tvSkus: TextView = view.findViewById(R.id.tvSkus)
    val llThreeSixty: LinearLayout = view.findViewById(R.id.llThreeSixty)
    val tvImages: TextView = view.findViewById(R.id.tvImages)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val tvPaid: TextView = view.findViewById(R.id.tvPaid)
    val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    val ivDownloadSKU: ImageView = view.findViewById(R.id.ivDownloadSKU)
    val cvMain: CardView = view.findViewById(R.id.cvMain)
    val flCategory: FrameLayout = view.findViewById(R.id.flCategory)
    val flSkus: FrameLayout = view.findViewById(R.id.flSkus)
    val flImages: FrameLayout = view.findViewById(R.id.flImages)
    val tvCategories: TextView = view.findViewById(R.id.tvCategories)
    val tvSku: TextView = view.findViewById(R.id.tvSku)
    val tvImage: TextView = view.findViewById(R.id.tvImage)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(context: Context,parent: ViewGroup): CompletedPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_completed_projects, parent, false)
            return CompletedPagedHolder(context,view)
        }
    }

    fun bind(item: Project?) {
        item?.let {
            showData(item)
        }
    }


    private fun showData(item: Project) {

        if (context.getString(R.string.app_name) == AppConstants.KARVI)
            ivDownloadSKU.visibility = View.INVISIBLE

        if (context.getString(R.string.app_name) == AppConstants.SWEEP) {
            apply {
                tvCategory.visibility = View.INVISIBLE
                tvSkus.visibility = View.INVISIBLE
                tvSkus.visibility = View.INVISIBLE
                flCategory.visibility = View.INVISIBLE
                flSkus.visibility = View.INVISIBLE
                flImages.visibility = View.INVISIBLE
                tvCategories.visibility = View.INVISIBLE
                tvSku.visibility = View.INVISIBLE
                tvImage.visibility = View.INVISIBLE
            }
        } else {
            tvCategory.text = item.categoryName
            llThreeSixty.visibility = View.GONE

            tvSkus.text = item.skuCount.toString()
            tvImages.text = item.imagesCount.toString()
        }

        try {
            if (item.thumbnail == null) {
                Glide.with(context)
                    .load(R.drawable.app_logo)
                    .into(ivThumbnail)
            } else {
                Glide.with(context) // replace with 'this' if it's in activity
                    .load(item.thumbnail)
                    .into(ivThumbnail)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }

        tvDate.text = getFormattedDate(getTimeStamp(item.createdOn))
        tvProjectName.text = item.projectName
        cvMain.setOnClickListener {

            if (item.skuCount == 0) {
                Toast.makeText(context, "No SKU data found", Toast.LENGTH_SHORT).show()
            } else {
                Intent(context, SkuPagedActivity::class.java)
                    .apply {
                        putExtra(AppConstants.STATUS,"completed")
                        putExtra("position", position)
                        putExtra(AppConstants.FROM_LOCAL_DB, true)
                        putExtra(AppConstants.PROJECT_NAME, item.projectName)
                        putExtra(AppConstants.SKU_COUNT, item.skuCount)
                        putExtra(AppConstants.PROJECT_UUIID, item.uuid)
                        putExtra(AppConstants.PROJECT_ID, item.projectId)
                        context.startActivity(this)
                    }
            }
        }
    }
}