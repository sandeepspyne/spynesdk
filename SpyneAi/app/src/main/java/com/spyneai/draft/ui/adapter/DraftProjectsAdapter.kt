package com.spyneai.draft.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.draft.ui.DraftSkusActivity
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity

class DraftProjectsAdapter(
    val context: Context,
    val draftsList: List<GetProjectsResponse.Project_data>
) : RecyclerView.Adapter<DraftProjectsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvSkus: TextView = view.findViewById(R.id.tvSkus)
        val llThreeSixty: LinearLayout = view.findViewById(R.id.llThreeSixty)
        val tvImages: TextView = view.findViewById(R.id.tvImages)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvPaid: TextView = view.findViewById(R.id.tvPaid)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val cvMain: CardView = view.findViewById(R.id.cvMain)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_draft_project, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (draftsList[position].categoryId == AppConstants.CARS_CATEGORY_ID && (draftsList[position].categoryId == draftsList[position].subCategoryId)) {
            holder.llThreeSixty.visibility = View.VISIBLE
            holder.tvCategory.text = "Automobiles"
        } else {
            holder.tvCategory.text = draftsList[position].category
            holder.llThreeSixty.visibility = View.GONE
        }

        try {
            if (draftsList[position].sku[0].images.isNullOrEmpty()) {
                if (draftsList[position].categoryId == AppConstants.CARS_CATEGORY_ID && (draftsList[position].categoryId == draftsList[position].subCategoryId)) {
                    Glide.with(context)
                        .load(R.drawable.three_sixty_thumbnail)
                        .into(holder.ivThumbnail)
                }else if (Utilities.getPreference(context, AppConstants.CATEGORY_NAME).equals("Food & Beverages")) {
                    Glide.with(context)
                        .load(R.drawable.ic_food_thumbnail_draft)
                        .into(holder.ivThumbnail)
                } else{
                    Glide.with(context)
                        .load(R.mipmap.defaults)
                        .into(holder.ivThumbnail)
                }
            }else {
                if (draftsList[position].sku[0].images[0].input_lres == null){
                    if (draftsList[position].categoryId == AppConstants.CARS_CATEGORY_ID && (draftsList[position].categoryId == draftsList[position].subCategoryId)) {
                        Glide.with(context)
                            .load(R.drawable.three_sixty_thumbnail)
                            .into(holder.ivThumbnail)
                    }else if (Utilities.getPreference(context, AppConstants.CATEGORY_NAME).equals("Food & Beverages")) {
                        Glide.with(context)
                            .load(R.drawable.ic_food_thumbnail_draft)
                            .into(holder.ivThumbnail)
                    } else{
                        Glide.with(context)
                            .load(R.mipmap.defaults)
                            .into(holder.ivThumbnail)
                    }
                }else{
                    context.loadSmartly(draftsList[position].sku[0].images[0].input_lres,
                        holder.ivThumbnail)
//                    Glide.with(context) // replace with 'this' if it's in activity
//                        .load(draftsList[position].sku[0].images[0].input_lres)
//                        .into(holder.ivThumbnail)
                }
            }
        }catch (e : Exception){
            e.printStackTrace()
        }catch (e : IndexOutOfBoundsException){
            e.printStackTrace()
        }

        holder.tvProjectName.text = draftsList[position].project_name
        holder.tvSkus.text = draftsList[position].total_sku.toString()
        holder.tvDate.text = draftsList[position].created_on
        holder.tvImages.text = draftsList[position].total_images.toString()

        holder.cvMain.setOnClickListener {
            if (draftsList[position].categoryId == draftsList[position].subCategoryId){
                Intent(context, DraftSkusActivity::class.java)
                    .apply {
                        putExtra("position", position)
                        putExtra(AppConstants.FROM_LOCAL_DB, false)
                        putExtra(AppConstants.PROJECT_NAME, draftsList[position].project_name)
                        putExtra(AppConstants.PROJECT_ID, draftsList[position].project_id)
                        context.startActivity(this)
                    }
            }else{
                when{
                    draftsList[position].sku.isNullOrEmpty() -> {
                        Intent(context, ShootActivity::class.java)
                            .apply {
                                putExtra(AppConstants.FROM_DRAFTS, true)
                                putExtra(AppConstants.CATEGORY_ID, draftsList[position].categoryId)
                                putExtra(AppConstants.CATEGORY_NAME, draftsList[position].category)
                                putExtra(AppConstants.PROJECT_ID, draftsList[position].project_id)
                                putExtra(AppConstants.SKU_NAME, draftsList[position].project_name)
                                putExtra(AppConstants.SKU_CREATED, false)
                                context.startActivity(this)
                            }
                    }

                    draftsList[position].category == "Footwear" && draftsList[position].sub_category == "" -> {
                        Utilities.savePrefrence(context, AppConstants.CATEGORY_NAME,draftsList[position].category)

                        Intent(context, ShootPortraitActivity::class.java)
                            .apply {
                                putExtra(AppConstants.FROM_DRAFTS, true)
                                putExtra(AppConstants.CATEGORY_NAME, draftsList[position].category)
                                putExtra(AppConstants.CATEGORY_ID, draftsList[position].categoryId)
                                putExtra(AppConstants.PROJECT_ID, draftsList[position].project_id)
                                putExtra(AppConstants.SKU_NAME, draftsList[position].project_name)
                                putExtra(AppConstants.SUB_CAT_NAME,draftsList[position].sub_category)
                                putExtra(AppConstants.SUB_CAT_ID, draftsList[position].subCategoryId)
                                putExtra(AppConstants.SKU_CREATED, true)
                                putExtra(AppConstants.FROM_DRAFTS, true)
                                putExtra(AppConstants.SKU_ID, draftsList[position].sku[0].sku_id)
                                context.startActivity(this)
                            }

                    }

                    else -> {
                        Intent(context, DraftSkusActivity::class.java)
                            .apply {
                                putExtra("position", position)
                                putExtra(AppConstants.FROM_LOCAL_DB, false)
                                putExtra(AppConstants.PROJECT_NAME, draftsList[position].project_name)
                                putExtra(AppConstants.PROJECT_ID, draftsList[position].project_id)
                                context.startActivity(this)
                            }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return draftsList?.size ?: 0
    }
}