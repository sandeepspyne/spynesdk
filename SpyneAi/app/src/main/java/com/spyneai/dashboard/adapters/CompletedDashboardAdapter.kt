package com.spyneai.dashboard.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.ShowImagesActivity
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.adapter.CompletedProjectAdapter
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.shoot.utils.log

class CompletedDashboardAdapter (
    val context: Context,
    val completedProjectList: ArrayList<CompletedSKUsResponse.Data>
) : RecyclerView.Adapter<CompletedDashboardAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvSku: TextView = view.findViewById(R.id.tvSku)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val clBackground: ConstraintLayout = view.findViewById(R.id.clBackground)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_completed_dashboard, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvSku.text = completedProjectList[position].sku_name
        holder.tvDate.text = completedProjectList[position].created_date
        Glide.with(context)
            .load(completedProjectList[position].thumbnail)
            .into(holder.ivImage)

        holder.clBackground.setOnClickListener{
            Utilities.savePrefrence(context,
                AppConstants.SKU_ID,
                completedProjectList[position].sku_id)
            log("Show Completed orders(sku_id): "+completedProjectList[position].sku_id)
            val intent = Intent(context,
                ShowImagesActivity::class.java)

            intent.putExtra("is_paid",completedProjectList[position].paid)
            context.startActivity(intent)
        }

    }

    override fun getItemCount() = completedProjectList.size


}