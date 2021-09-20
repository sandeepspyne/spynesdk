package com.spyneai.orders.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.activity.OngoingSkusActivity

class MyOngoingProjectAdapter(
    val context: Context,
    val getProjectList: List<GetProjectsResponse.Project_data>,
    val viewModel: MyOrdersViewModel
) : RecyclerView.Adapter<MyOngoingProjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyOngoingProjectAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing_projects, parent, false)
        return MyOngoingProjectAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyOngoingProjectAdapter.ViewHolder, position: Int) {



        if (getProjectList[position].sub_category == "360_exterior"
            || getProjectList[position].sub_category.equals("360_interior")
        ) {
            holder.llThreeSixty.visibility = View.VISIBLE
            holder.tvCategory.text = "Automobiles"
        } else {
            holder.tvCategory.text = getProjectList[position].category
            holder.llThreeSixty.visibility = View.GONE
        }

        if (context.getString(R.string.app_name) == AppConstants.SWEEP){
            holder.apply {
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
            }
        }else{
            if (getProjectList[position].category.equals("cat_d8R14zUNx")
                || getProjectList[position].category.equals("cat_Ujt0kuFxY")
                || getProjectList[position].category.equals("cat_Ujt0kuFxX")
                || getProjectList[position].category.equals("E-Commerce")
                || getProjectList[position].category.equals("Footwear")
                || getProjectList[position].category.equals("Bikes")
            ){
                when (context.getString(R.string.app_name)){
                    AppConstants.SWIGGYINSTAMART, AppConstants.FLIPKART_GROCERY ->
                        holder.tvImageCount.visibility = View.VISIBLE
                    else ->
                        holder.tvImageCount.visibility = View.INVISIBLE
                }
            }else{
                holder.tvImageCount.visibility = View.VISIBLE
            }

        try {
            if (getProjectList[position].sku[0].images.isNullOrEmpty()) {
                if (getProjectList[position].sub_category == "360_exterior"
                    || getProjectList[position].sub_category.equals("360_interior")){
                    Glide.with(context)
                        .load(R.drawable.three_sixty_thumbnail)
                        .into(holder.ivThumbnail)
                }else {
                    Glide.with(context)
                        .load(R.mipmap.defaults)
                        .into(holder.ivThumbnail)
                }
            }else {
                if (getProjectList[position].sku[0].images[0].input_lres == null){
                    if (getProjectList[position].sub_category == "360_exterior"
                        || getProjectList[position].sub_category.equals("360_interior")){
                        Glide.with(context)
                            .load(R.drawable.three_sixty_thumbnail)
                            .into(holder.ivThumbnail)
                    }else {
                        Glide.with(context)
                            .load(R.mipmap.defaults)
                            .into(holder.ivThumbnail)
                    }
                }else{
                    Glide.with(context) // replace with 'this' if it's in activity
                        .load(getProjectList[position].sku[0].images[0].input_lres)
                        .into(holder.ivThumbnail)
                }

            }
        }catch (e : Exception){
            e.printStackTrace()
        }catch (e : IndexOutOfBoundsException){
            e.printStackTrace()
        }

            if (getProjectList[position].status.equals("Uploaded")) {
                when (context.getString(R.string.app_name)) {
                    AppConstants.SWIGGYINSTAMART, AppConstants.FLIPKART_GROCERY ->{
                        holder.tvImageCount.visibility = View.VISIBLE
                        holder.lottieProgressCircle.visibility = View.VISIBLE
                        holder.llUploaded.visibility = View.INVISIBLE
                    } else -> {
                    holder.tvImageCount.visibility = View.INVISIBLE
                    holder.lottieProgressCircle.visibility = View.INVISIBLE
                    holder.llUploaded.visibility = View.VISIBLE
                    }
                }
            }
        }

        holder.tvSkus.text = getProjectList[position].total_sku.toString()

        holder.tvImages.text = getProjectList[position].total_images.toString()

        holder.tvImageCount.text =
            getProjectList[position].processed_images.toString() + "/" + getProjectList[position].total_images.toString()

        holder.tvProjectName.text = getProjectList[position].project_name
        holder.tvDate.text = getProjectList[position].created_on

        try {
            if (getProjectList[position].sku[0].images.isNullOrEmpty()) {
                if (getProjectList[position].sub_category == "360_exterior"
                    || getProjectList[position].sub_category.equals("360_interior")){
                    Glide.with(context)
                        .load(R.drawable.three_sixty_thumbnail)
                        .into(holder.ivThumbnail)
                }else {
                    Glide.with(context)
                        .load(R.mipmap.defaults)
                        .into(holder.ivThumbnail)
                }
            }else {
                if (getProjectList[position].sku[0].images[0].input_lres == null){
                    if (getProjectList[position].sub_category == "360_exterior"
                        || getProjectList[position].sub_category.equals("360_interior")){
                        Glide.with(context)
                            .load(R.drawable.three_sixty_thumbnail)
                            .into(holder.ivThumbnail)
                    }else {
                        Glide.with(context)
                            .load(R.mipmap.defaults)
                            .into(holder.ivThumbnail)
                    }
                }else{
                    Glide.with(context) // replace with 'this' if it's in activity
                        .load(getProjectList[position].sku[0].images[0].input_lres)
                        .into(holder.ivThumbnail)
                }

            }
        }catch (e : Exception){
            e.printStackTrace()
        }catch (e : IndexOutOfBoundsException){
            e.printStackTrace()
        }


        holder.cvMain.setOnClickListener {

            if (getProjectList[position].category.equals("cat_d8R14zUNE") || getProjectList[position].category.equals("Automobiles")){

            }else{
                if (getProjectList[position].sku.isNullOrEmpty()){
                    Toast.makeText(context, "No SKU data found", Toast.LENGTH_SHORT).show()
                }else if (getProjectList[position].status.equals("Uploaded")){
                    Intent(context, OngoingSkusActivity::class.java)
                        .apply {
                            putExtra("position", position)
                            context.startActivity(this)
                        }
                }


            }
        }

    }

    override fun getItemCount(): Int {
        return getProjectList.size
    }
}