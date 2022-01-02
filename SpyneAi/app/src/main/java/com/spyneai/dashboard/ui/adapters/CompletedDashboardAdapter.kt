package com.spyneai.dashboard.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.ui.activity.CompletedSkusActivity
import com.spyneai.shoot.utils.log
import com.spyneai.threesixty.ui.ThreeSixtyExteriorActivity

class CompletedDashboardAdapter(
    val context: Context,
    val completedProjectList: ArrayList<GetProjectsResponse.Project_data>
) : RecyclerView.Adapter<CompletedDashboardAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvProject: TextView = view.findViewById(R.id.tvProject)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val clBackground: ConstraintLayout = view.findViewById(R.id.clBackground)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_completed_dashboard, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvProject.text = completedProjectList[position].project_name
        holder.tvDate.text = completedProjectList[position].created_on


        try {
            if (completedProjectList[position].sku[0].images.isNullOrEmpty()) {
                if (completedProjectList[position].sub_category == "360_exterior"
                    || completedProjectList[position].sub_category.equals("360_interior")){
                    Glide.with(context)
                        .load(R.drawable.three_sixty_thumbnail)
                        .into(holder.ivImage)
                }else {
                    Glide.with(context)
                        .load(R.mipmap.defaults)
                        .into(holder.ivImage)
                }
            }else {
                if (completedProjectList[position].sku[0].images[0].input_lres == null){
                    if (completedProjectList[position].sub_category == "360_exterior"
                        || completedProjectList[position].sub_category.equals("360_interior")){
                        Glide.with(context)
                            .load(R.drawable.three_sixty_thumbnail)
                            .into(holder.ivImage)
                    }else {
                        Glide.with(context)
                            .load(R.mipmap.defaults)
                            .into(holder.ivImage)
                    }
                }else{
                    Glide.with(context) // replace with 'this' if it's in activity
                        .load(completedProjectList[position].sku[0].images[0].input_lres)
                        .into(holder.ivImage)
                }

            }
        }catch (e : Exception){
            e.printStackTrace()
        }catch (e : IndexOutOfBoundsException){
            e.printStackTrace()
        }

        holder.clBackground.setOnClickListener{
            Utilities.savePrefrence(context,
                AppConstants.SKU_ID,
                completedProjectList[position].sku[0].sku_id)

            log("Show Completed orders(sku_id): "+completedProjectList[position].sku[0].sku_id)

            if (completedProjectList[position].sub_category.equals("360_interior") || completedProjectList[position].sub_category.equals("360_exterior")){
                Intent(context,ThreeSixtyExteriorActivity::class.java)
                    .apply {
                        putExtra("sku_id",completedProjectList[position].sku[0].sku_id)
                        context.startActivity(this)
                    }
            }else{

                if (completedProjectList[position].sku.isNullOrEmpty()){
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

    override fun getItemCount() = completedProjectList.size


}