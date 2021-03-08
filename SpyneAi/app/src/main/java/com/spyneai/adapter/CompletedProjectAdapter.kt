package com.spyneai.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.model.categories.Data
import com.spyneai.model.projects.CompletedProjectResponse

class CompletedProjectAdapter (
        val context: Context,
        val completedProjectList: ArrayList<CompletedProjectResponse>,
        val btnlistener: BtnClickListener)
    : RecyclerView.Adapter<CompletedProjectAdapter.ViewHolder>() {


    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val rlSkuGifList: RelativeLayout = view.findViewById(R.id.rlSkuGifList)
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvName: TextView = view.findViewById(R.id.tvName)
//        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvDate: TextView = view.findViewById(R.id.tvDate)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_completed_projects, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = completedProjectList[position].sku_name
        holder.tvDate.text = completedProjectList[position].created_at
        Glide.with(context)
                .load(completedProjectList[position].output_image_url)
                .into(holder.ivImage)

        mClickListener = btnlistener
        holder.rlSkuGifList.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    override fun getItemCount() = completedProjectList.size


}