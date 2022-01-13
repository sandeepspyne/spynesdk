package com.spyneai.processedimages.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.OnItemClickListener
import com.spyneai.shoot.repository.model.image.Image



class ProcessedImagesAdapter(
    val context: Context,
    val imageList: ArrayList<Image>,
    val listener : OnItemClickListener
) : RecyclerView.Adapter<ProcessedImagesAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvImageType: TextView? = view.findViewById(R.id.tvImageType)
        val ivProcessed: ImageView = view.findViewById(R.id.ivProcessed)

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0
        else{
            if (imageList[position].image_category == imageList[position - 1].image_category) 1 else 0
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        // Create a new view, which defines the UI of the list item

        val view = if (viewType == 0){
            LayoutInflater.from(context)
                .inflate(R.layout.item_processed_image_with_type, viewGroup, false)
        }else{
            LayoutInflater.from(context)
                .inflate(R.layout.item_processed_image, viewGroup, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.tvImageType != null)
                holder.tvImageType.text = imageList[position].image_category

        Glide.with(context) // replace with 'this' if it's in activity
            .load(imageList[position].input_image_hres_url)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.ivProcessed)

        holder.ivProcessed.setOnClickListener {
            listener.onItemClick(holder.ivProcessed,position,imageList[position])
        }

    }

    override fun getItemCount() = imageList.size
}