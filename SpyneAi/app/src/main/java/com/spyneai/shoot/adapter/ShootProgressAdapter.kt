package com.spyneai.shoot.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.shoot.data.model.ShootProgress


class ShootProgressAdapter(
    val context: Context, private var shootProgressList: ArrayList<ShootProgress>)
    : RecyclerView.Adapter<ShootProgressAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProgress: ImageView = view.findViewById(R.id.ivProgress)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_progress, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (shootProgressList[position].isSelected)
            viewHolder.ivProgress.setImageResource(R.mipmap.progress_orang)
        else
            viewHolder.ivProgress.setImageResource(R.mipmap.progress_grey)
    }

    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount() = if (shootProgressList == null) 0 else shootProgressList.size

    fun updateList(shootProgressList: ArrayList<ShootProgress>) {
        this.shootProgressList = shootProgressList
        notifyDataSetChanged()
    }
}