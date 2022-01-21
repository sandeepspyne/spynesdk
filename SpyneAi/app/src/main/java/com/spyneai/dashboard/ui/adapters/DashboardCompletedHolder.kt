package com.spyneai.dashboard.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.getFormattedDate
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.orders.ui.activity.CompletedSkusActivity
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.utils.log
import com.spyneai.threesixty.ui.ThreeSixtyExteriorActivity

class DashboardCompletedHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {

    val ivImage: ImageView = view.findViewById(R.id.ivImage)
    val tvProject: TextView = view.findViewById(R.id.tvProject)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val clBackground: ConstraintLayout = view.findViewById(R.id.clBackground)
    
    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            parent: ViewGroup
        ): DashboardCompletedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.rv_completed_dashboard, parent, false)
            return DashboardCompletedHolder(context, view)
        }
    }

    fun bind(item: Project?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Project) {
        tvProject.text = item.projectName
        tvDate.text = getFormattedDate(item.createdOn)


        try {
            if (item.imagesCount == 0) {
                if (item.subCategoryName == "360_exterior"
                    || item.subCategoryName.equals("360_interior")){
                    Glide.with(context)
                        .load(R.drawable.three_sixty_thumbnail)
                        .into(ivImage)
                }else {
                    Glide.with(context)
                        .load(R.mipmap.defaults)
                        .into(ivImage)
                }
            }else {
                if (item.thumbnail == null){
                    if (item.subCategoryName == "360_exterior"
                        || item.subCategoryName.equals("360_interior")){
                        Glide.with(context)
                            .load(R.drawable.three_sixty_thumbnail)
                            .into(ivImage)
                    }else {
                        Glide.with(context)
                            .load(R.mipmap.defaults)
                            .into(ivImage)
                    }
                }else{
                    Glide.with(context) // replace with 'this' if it's in activity
                        .load(item.thumbnail)
                        .into(ivImage)
                }

            }
        }catch (e : Exception){
            e.printStackTrace()
        }catch (e : IndexOutOfBoundsException){
            e.printStackTrace()
        }

        clBackground.setOnClickListener{

            if (item.subCategoryName.equals("360_interior") || item.subCategoryName.equals("360_exterior")){
//                Intent(context, ThreeSixtyExteriorActivity::class.java)
//                    .apply {
//                        putExtra("sku_id",item.sku[0].sku_id)
//                        context.startActivity(this)
//                    }
            }else{

                if (item.skuCount == 0){
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
}