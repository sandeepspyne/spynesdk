package com.spyneai.dashboard.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.ui.MyOrdersActivity

class OngoingDashboardAdapter(
    val context: Context,
    val ongoingProjectList: ArrayList<GetProjectsResponse.Project_data>,
) : RecyclerView.Adapter<OngoingDashboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvProject: TextView = view.findViewById(R.id.tvProject)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val clBackground: ConstraintLayout = view.findViewById(R.id.clBackground)
        val llFailed: LinearLayout = view.findViewById(R.id.llFailed)
        val llOngoing: LinearLayout = view.findViewById(R.id.llOngoing)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_ongoing_dashboard, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.tvSku.text = ongoingProjectList[position].category
//        holder.tvDate.text = ongoingProjectList[position].sku_name

        holder.tvProject.text = ongoingProjectList[position].project_name
        holder.tvDate.text = ongoingProjectList[position].created_on

        try {
            if (ongoingProjectList[position].sku[0].images.isNullOrEmpty()) {
                if (ongoingProjectList[position].sub_category == "360_exterior"
                    || ongoingProjectList[position].sub_category.equals("360_interior")){
                    Glide.with(context)
                        .load(R.drawable.three_sixty_thumbnail)
                        .into(holder.ivImage)
                }else {
                    Glide.with(context)
                        .load(R.mipmap.defaults)
                        .into(holder.ivImage)
                }
            }else {
                if (ongoingProjectList[position].sku[0].images[0].input_lres == null){
                    if (ongoingProjectList[position].sub_category == "360_exterior"
                        || ongoingProjectList[position].sub_category.equals("360_interior")){
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
                        .load(ongoingProjectList[position].sku[0].images[0].input_lres)
                        .into(holder.ivImage)
                }

            }
        }catch (e : Exception){
            e.printStackTrace()
        }catch (e : IndexOutOfBoundsException){
            e.printStackTrace()
        }




        holder.clBackground.setOnClickListener {
            val intent = Intent(context, MyOrdersActivity::class.java)
            intent.putExtra("TAB_ID", 1)
            context.startActivity(intent)
        }

//                    Utilities.savePrefrence(context,
//                        AppConstants.SKU_ID,
//                        ongoingProjectList[position].skuId)
    }

    override fun getItemCount() = ongoingProjectList.size

}


