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
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.getFormattedDate
import com.spyneai.getTimeStamp
import com.spyneai.needs.AppConstants
import com.spyneai.orders.ui.activity.SkuPagedActivity
import com.spyneai.shoot.repository.model.project.Project

@ExperimentalPagingApi
class OngoingPagedHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {

    val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
    val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    val tvSkus: TextView = view.findViewById(R.id.tvSkus)
    val llThreeSixty: LinearLayout = view.findViewById(R.id.llThreeSixty)
    val tvImages: TextView = view.findViewById(R.id.tvImages)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    val tvImageCount: TextView = view.findViewById(R.id.tvImageCount)
    val lottieProgressCircle: LottieAnimationView = view.findViewById(R.id.lottieProgressCircle)
    val llUploaded: LinearLayout = view.findViewById(R.id.llUploaded)
    val cvMain: CardView = view.findViewById(R.id.cvMain)
    val flAnimationContainer: FrameLayout = view.findViewById(R.id.flAnimationContainer)
    val flCategory: FrameLayout = view.findViewById(R.id.flCategory)
    val flSkus: FrameLayout = view.findViewById(R.id.flSkus)
    val flImages: FrameLayout = view.findViewById(R.id.flImages)
    val tvCategories: TextView = view.findViewById(R.id.tvCategories)
    val tvSku: TextView = view.findViewById(R.id.tvSku)
    val tvImage: TextView = view.findViewById(R.id.tvImage)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            parent: ViewGroup
        ): OngoingPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_ongoing_projects, parent, false)
            return OngoingPagedHolder(context, view)
        }
    }

    fun bind(item: Project?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Project) {
        llThreeSixty.visibility = View.GONE
        tvCategory.text = item.categoryName

        if (context.getString(R.string.app_name) == AppConstants.SWEEP) {
            tvCategory.visibility = View.INVISIBLE
            tvSkus.visibility = View.INVISIBLE
            tvSkus.visibility = View.INVISIBLE
            flCategory.visibility = View.INVISIBLE
            flSkus.visibility = View.INVISIBLE
            flImages.visibility = View.INVISIBLE
            tvCategories.visibility = View.INVISIBLE
            tvSku.visibility = View.INVISIBLE
            tvImage.visibility = View.INVISIBLE
            tvImages.visibility = View.INVISIBLE
        } else {
            if (item.categoryName.equals("cat_d8R14zUNx")
                || item.categoryName.equals("cat_Ujt0kuFxY")
                || item.categoryName.equals("cat_Ujt0kuFxX")
                || item.categoryName.equals("E-Commerce")
                || item.categoryName.equals("Footwear")
                || item.categoryName.equals("Bikes")
            ) {
                when (context.getString(R.string.app_name)) {
                    AppConstants.SWIGGYINSTAMART, AppConstants.SPYNE_AI, AppConstants.FLIPKART_GROCERY, AppConstants.EBAY, AppConstants.UDAAN ->
                        tvImageCount.visibility = View.VISIBLE
                    else ->
                        tvImageCount.visibility = View.INVISIBLE
                }
            } else {
                tvImageCount.visibility = View.VISIBLE
            }

            if (item.status.equals("Uploaded")) {
                when (context.getString(R.string.app_name)) {
                    AppConstants.SWIGGYINSTAMART, AppConstants.FLIPKART_GROCERY, AppConstants.EBAY, AppConstants.UDAAN -> {
                        tvImageCount.visibility = View.VISIBLE
                        lottieProgressCircle.visibility = View.VISIBLE
                        llUploaded.visibility = View.INVISIBLE
                    }
                    else -> {
                        tvImageCount.visibility = View.INVISIBLE
                        lottieProgressCircle.visibility = View.INVISIBLE
                        llUploaded.visibility = View.VISIBLE
                    }
                }
            }
        }

        tvSkus.text = item.skuCount.toString()

        tvImages.text = item.imagesCount.toString()
        tvImageCount.text = item.processedCount.toString() + "/" + item.imagesCount.toString()

        tvProjectName.text = item.projectName
        if(item.processedCount == 0){
            tvDate.text = getFormattedDate(item.createdOn.toLong())
        }else{
            tvDate.text = getFormattedDate(getTimeStamp(item.createdOn))
        }
        //need thumbnal
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

        cvMain.setOnClickListener {

            if (item.skuCount == 0) {
                Toast.makeText(context, "No SKU data found", Toast.LENGTH_SHORT).show()
            } else {
                Intent(context, SkuPagedActivity::class.java)
                    .apply {
                        putExtra(AppConstants.STATUS, "ongoing")
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