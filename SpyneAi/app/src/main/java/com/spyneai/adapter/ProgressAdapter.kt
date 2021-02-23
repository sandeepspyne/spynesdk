package com.spyneai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R

class ProgressAdapter(
        val context: Context,val framesList: ArrayList<Int>)
    : RecyclerView.Adapter<ProgressAdapter.ViewHolder>() {

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

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

    //    for (i in 0..framesList.size)
        if (framesList[position] == 0)
            viewHolder.ivProgress.setImageResource(R.mipmap.progress_orang)
        else
            viewHolder.ivProgress.setImageResource(R.mipmap.progress_grey)

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = framesList.size
}
