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
import com.spyneai.needs.AppConstants
import com.spyneai.orders.ui.activity.OngoingSkusActivity
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
        if (item.categoryId == AppConstants.CARS_CATEGORY_ID && (item.subCategoryId == item.categoryId)) {
            llThreeSixty.visibility = View.VISIBLE
            tvCategory.text = "Automobiles"
        } else {
            llThreeSixty.visibility = View.GONE
            tvCategory.text = item.categoryName
        }


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

        //tvImages.text = item.total_images.toString()
//        tvImageCount.text =
//            item.processed_images.toString() + "/" + item.total_images.toString()

        tvProjectName.text = item.projectName
        tvDate.text = item.createdOn

        //need thumbnal
//        try {
//            if (item.sku[0].images.isNullOrEmpty()) {
//                if (item.categoryId == AppConstants.CARS_CATEGORY_ID && (item.categoryId == item.subCategoryId)){
//                    Glide.with(context)
//                        .load(R.drawable.three_sixty_thumbnail)
//                        .into(ivThumbnail)
//                }else {
//                    Glide.with(context)
//                        .load(R.mipmap.defaults)
//                        .into(ivThumbnail)
//                }
//            }else {
//                if (item.sku[0].images[0].input_lres == null){
//                    if (item.categoryId == AppConstants.CARS_CATEGORY_ID && (item.categoryId == item.subCategoryId)){
//                        Glide.with(context)
//                            .load(R.drawable.three_sixty_thumbnail)
//                            .into(ivThumbnail)
//                    }else {
//                        Glide.with(context)
//                            .load(R.mipmap.defaults)
//                            .into(ivThumbnail)
//                    }
//                }else{
//                    Glide.with(context) // replace with 'this' if it's in activity
//                        .load(item.sku[0].images[0].input_lres)
//                        .into(ivThumbnail)
//                }
//
//            }
//        }catch (e : Exception){
//            e.printStackTrace()
//        }catch (e : IndexOutOfBoundsException){
//            e.printStackTrace()
//        }

        cvMain.setOnClickListener {

            if (item.categoryName.equals("cat_d8R14zUNE") || item.categoryName.equals("Automobiles")) {

            } else {
                if (item.skuCount == 0) {
                    Toast.makeText(context, "No SKU data found", Toast.LENGTH_SHORT).show()
                } else if (item.status.equals("Uploaded")) {
                    Intent(context, OngoingSkusActivity::class.java)
                        .apply {
                            putExtra("position", position)
                            context.startActivity(this)
                        }
                }
            }
        }
    }
}