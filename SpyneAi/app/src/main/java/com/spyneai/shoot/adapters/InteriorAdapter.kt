package com.spyneai.shoot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.dashboard.response.NewSubCatResponse

class InteriorAdapter (val context: Context,
                       val interiorList: ArrayList<NewSubCatResponse.Interior>)
    : RecyclerView.Adapter<InteriorAdapter.ViewHolder>() {

    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardIFrameImages: CardView = view.findViewById(R.id.cardIFrameImages)
        val ivIFrameImages: ImageView = view.findViewById(R.id.ivIFrameImages)
        val tvIFrameImages: TextView = view.findViewById(R.id.tvIFrameImages)
    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.sub_frames, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Glide.with(context).load(
                interiorList[position].display_thumbnail)
            .into(viewHolder.ivIFrameImages)

        viewHolder.tvIFrameImages.text = interiorList[position].display_name

        if (interiorList[position].isSelected)
            viewHolder.cardIFrameImages.setBackgroundResource(R.drawable.bg_selected)
        else
            viewHolder.cardIFrameImages.setBackgroundResource(R.drawable.bg_channel)

    }

    override fun getItemCount() = interiorList.size

}
