package com.spyneai.orders.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.activity.CompletedSkusActivity
import com.spyneai.threesixty.ui.ThreeSixtyExteriorActivity

class MyCompletedProjectsAdapter(
    val context: Context,
    val getProjectList: List<GetProjectsResponse.Project_data>,
    val viewModel: MyOrdersViewModel
) : RecyclerView.Adapter<MyCompletedProjectsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_projects, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCompletedProjectsAdapter.ViewHolder, position: Int) {

        if (context.getString(R.string.app_name) == AppConstants.KARVI)
            holder.ivDownloadSKU.visibility = View.INVISIBLE

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
                //ivDownloadSKU.visibility = View.INVISIBLE
            }
        }else{
            if (getProjectList[position].categoryId == AppConstants.CARS_CATEGORY_ID && (getProjectList[position].categoryId == getProjectList[position].subCategoryId)){
                holder.llThreeSixty.visibility = View.VISIBLE
                holder.tvCategory.text = "Automobiles"
            }else{
                holder.tvCategory.text = getProjectList[position].category
                holder.llThreeSixty.visibility = View.GONE
            }


            holder.tvSkus.text = getProjectList[position].total_sku.toString()
            holder.tvImages.text = getProjectList[position].total_images.toString()
        }

        try {
            if (getProjectList[position].sku[0].images.isNullOrEmpty()) {
                if (getProjectList[position].categoryId == AppConstants.CARS_CATEGORY_ID && (getProjectList[position].categoryId == getProjectList[position].subCategoryId)){
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
                    if (getProjectList[position].categoryId == AppConstants.CARS_CATEGORY_ID && (getProjectList[position].categoryId == getProjectList[position].subCategoryId)){
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

        holder.tvDate.text = getProjectList[position].created_on
        holder.tvProjectName.text = getProjectList[position].project_name
        holder.cvMain.setOnClickListener {
            viewModel.position.value = position
            viewModel.projectItemClicked.value = true

            if (getProjectList[position].categoryId == AppConstants.CARS_CATEGORY_ID && (getProjectList[position].categoryId == getProjectList[position].subCategoryId)){
                Intent(context,ThreeSixtyExteriorActivity::class.java)
                    .apply {
                        putExtra("sku_id",getProjectList[position].sku[0].sku_id)
                        context.startActivity(this)
                    }
            }else{

                if (getProjectList[position].sku.isNullOrEmpty()){
                    Toast.makeText(context, "No SKU data found", Toast.LENGTH_SHORT).show()
                }else{
                    Intent(context, CompletedSkusActivity::class.java)
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