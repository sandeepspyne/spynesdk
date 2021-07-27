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
            Glide.with(context)
                .load(ongoingProjectList[position].sku[0].images[0].input_lres)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(holder.ivImage)
        }catch (e: Exception){

        }

//        if (ongoingProjectList[position].isFailure){
//            holder.llFailed.visibility = View.VISIBLE
//            holder.llOngoing.visibility = View.GONE
//        }


        holder.clBackground.setOnClickListener {
            val intent = Intent(context, OngoingOrdersActivity::class.java)
            context.startActivity(intent)
        }

//                    Utilities.savePrefrence(context,
//                        AppConstants.SKU_ID,
//                        ongoingProjectList[position].skuId)
    }

    override fun getItemCount() = ongoingProjectList.size

}


