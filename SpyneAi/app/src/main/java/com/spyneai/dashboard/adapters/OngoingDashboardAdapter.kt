package com.spyneai.dashboard.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.activity.ShowImagesActivity
import com.spyneai.adapter.OngoingProjectAdapter
import com.spyneai.model.processImageService.Task
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class OngoingDashboardAdapter (
    val context: Context,
    val ongoingProjectList: ArrayList<Task>,
) : RecyclerView.Adapter<OngoingDashboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvSku: TextView = view.findViewById(R.id.tvSku)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_ongoing_dashboard, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvSku.text = ongoingProjectList[position].catName

        Glide.with(context)
            .load(ongoingProjectList[position].imageFileList[0])
            .into(holder.ivImage)

        holder.ivImage.setOnClickListener {
            val intent = Intent(context, OngoingOrdersActivity::class.java)
            context.startActivity(intent)
        }

                    Utilities.savePrefrence(context,
                        AppConstants.SKU_ID,
                        ongoingProjectList[position].skuId)
    }
    override fun getItemCount() = ongoingProjectList.size

}


