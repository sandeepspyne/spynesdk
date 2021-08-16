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
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.shoot.data.model.Project
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.threesixty.ui.ThreeSixtyExteriorActivity

class LocalDraftProjectsAdapter(
val context: Context,
val draftsList: List<Project>
) : RecyclerView.Adapter<LocalDraftProjectsAdapter.ViewHolder>() {

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

        if (draftsList[position].subCategoryName == "360_exterior"
            || draftsList[position].subCategoryName.equals("360_interior")
        ){
            holder.llThreeSixty.visibility = View.VISIBLE
            holder.tvCategory.text = "Automobiles"

            Glide.with(context)
                .load(R.drawable.three_sixty_thumbnail)
                .into(holder.ivThumbnail)
        }else{
            holder.tvCategory.text = draftsList[position].categoryName
            holder.llThreeSixty.visibility = View.GONE

            if (draftsList[position].thumbnail != null){
                Glide.with(context)
                    .load(draftsList[position].thumbnail)
                    .into(holder.ivThumbnail)
            }
        }

        holder.tvProjectName.text = draftsList[position].projectName
        holder.tvSkus.text = draftsList[position].skus.toString()
        holder.tvDate.text = draftsList[position].createdOn.toString()
        holder.tvImages.text = draftsList[position].images.toString()

        holder.cvMain.setOnClickListener {
            if (draftsList[position].subCategoryName.equals("360_interior") || draftsList[position].subCategoryName.equals("360_exterior")){
//                Intent(context, ThreeSixtyExteriorActivity::class.java)
//                    .apply {
//                        putExtra("sku_id",draftsList[position].s)
//                        context.startActivity(this)
//                    }
            }else{

                if (draftsList[position].skus == 0){
                    Intent(context, ShootActivity::class.java)
                        .apply {
                            putExtra(AppConstants.FROM_DRAFTS, true)
                            putExtra(AppConstants.CATEGORY_ID, draftsList[position].categoryId)
                            putExtra(AppConstants.CATEGORY_NAME, draftsList[position].categoryName)
                            putExtra(AppConstants.PROJECT_ID, draftsList[position].projectId)
                            putExtra(AppConstants.SKU_NAME, draftsList[position].projectName)
                            putExtra(AppConstants.SKU_CREATED, false)
                            context.startActivity(this)
                        }
                }else{
                    Intent(context, DraftSkusActivity::class.java)
                        .apply {
                            putExtra("position", position)
                            putExtra(AppConstants.FROM_LOCAL_DB, true)
                            putExtra(AppConstants.PROJECT_NAME, draftsList[position].projectName)
                            putExtra(AppConstants.PROJECT_ID, draftsList[position].projectId)
                            context.startActivity(this)
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return draftsList?.size ?: 0
    }
}