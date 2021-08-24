package com.spyneai.draft.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.draft.ui.DraftSkuDetailsActivity
import com.spyneai.draft.ui.DraftSkusActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.SkusAdapter

import com.spyneai.processedimages.ui.ShowImagesActivity
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.threesixty.ui.ThreeSixtyActivity

class DraftSkusAdapter (
    val context: Context,
    val projectId : String,
    val categoryName : String,
    val categoryId : String,
    val skuList: ArrayList<GetProjectsResponse.Sku>
) : RecyclerView.Adapter<DraftSkusAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvImages: TextView = view.findViewById(R.id.tvImages)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val cvMain: CardView = view.findViewById(R.id.cvMain)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_draft_sku, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCategory.text = skuList[position].subCategory
        holder.tvDate.text = skuList[position].created_on

        try {
            Glide.with(context) // replace with 'this' if it's in activity
                .load(skuList[position].images[0].input_lres)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(holder.ivThumbnail)

        }catch (e: Exception){

        }

        if (skuList[position].categoryId == "360_exterior"
            || skuList[position].categoryId.equals("360_interior")
        ){
            Glide.with(context)
                .load(R.drawable.three_sixty_thumbnail)
                .into(holder.ivThumbnail)
        }

        holder.tvSkuName.text = skuList[position].sku_name
        holder.tvImages.text = skuList[position].total_images.toString()


        holder.cvMain.setOnClickListener {
            Utilities.savePrefrence(
                context,
                AppConstants.SKU_ID,
                skuList[position].sku_id
            )

            if (skuList[position].categoryId == "360_exterior"){
                val threeeSixtyIntent = Intent(
                    context,
                    ThreeSixtyActivity::class.java
                ).apply {
                    putExtra(AppConstants.FROM_LOCAL_DB, true)
                    putExtra(AppConstants.FROM_DRAFTS, true)
                    putExtra(AppConstants.PROJECT_ID, projectId)
                    putExtra(AppConstants.CATEGORY_ID, skuList[position].categoryId)
                    putExtra(AppConstants.SUB_CAT_ID,skuList[position].subCategoryId)
                    putExtra(AppConstants.SUB_CAT_NAME,skuList[position].subCategory)
                    putExtra(AppConstants.CATEGORY_NAME, skuList[position].category)
                    putExtra(AppConstants.SKU_NAME, skuList[position].sku_name)
                    putExtra(AppConstants.PROJECT_NAME, skuList[position].sku_name)
                    putExtra(AppConstants.SKU_COUNT, skuList.size)
                    putExtra(AppConstants.SKU_CREATED, false)
                    putExtra(AppConstants.SKU_ID, skuList[position].sku_id)
                    putExtra(AppConstants.EXTERIOR_ANGLES, skuList[position].exteriorClicks)
                    //putExtra("is_paid",skuList[position].paid)
                    //putExtra(AppConstants.IMAGE_TYPE,skuList[position].category)
                    putExtra(AppConstants.IS_360,skuList[position].is360)
                }

                context.startActivity(threeeSixtyIntent)
            }else {
                Intent(
                    context,
                    DraftSkuDetailsActivity::class.java
                ).apply {
                    putExtra(AppConstants.FROM_LOCAL_DB, false)
                    putExtra(AppConstants.FROM_DRAFTS, true)
                    putExtra(AppConstants.PROJECT_ID,projectId)
                    putExtra(AppConstants.CATEGORY_NAME, skuList[position].category)
                    putExtra(AppConstants.CATEGORY_ID, skuList[position].categoryId)
                    putExtra(AppConstants.SUB_CAT_ID,skuList[position].subCategoryId)
                    putExtra(AppConstants.SUB_CAT_NAME,skuList[position].subCategory)
                    putExtra(AppConstants.SKU_NAME, skuList[position].sku_name)
                    putExtra(AppConstants.PROJECT_NAME, skuList[position].sku_name)
                    putExtra(AppConstants.SKU_COUNT, skuList.size)
                    putExtra(AppConstants.SKU_CREATED, false)
                    putExtra(AppConstants.SKU_ID, skuList[position].sku_id)
                    putExtra(AppConstants.EXTERIOR_ANGLES, skuList[position].exteriorClicks)
                    //putExtra("is_paid",skuList[position].paid)
                    //putExtra(AppConstants.IMAGE_TYPE,skuList[position].category)
                    putExtra(AppConstants.IS_360,skuList[position].is360)
                    context.startActivity(this)

                    context.startActivity(this)
                }
            }



        }


    }

    override fun getItemCount(): Int {
        return skuList.size
    }
}