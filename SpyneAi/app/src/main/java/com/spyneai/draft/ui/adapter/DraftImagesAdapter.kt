package com.spyneai.draft.ui.adapter

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
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.adapter.ProcessedImagesAdapter

class DraftImagesAdapter(
    val context: Context,
    val imageList: ArrayList<ImagesOfSkuRes.Data>
) : RecyclerView.Adapter<DraftImagesAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProcessed: ImageView = view.findViewById(R.id.ivProcessed)
    }


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        // Create a new view, which defines the UI of the list item

        val view =  LayoutInflater.from(context)
            .inflate(R.layout.item_draft_images, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(context) // replace with 'this' if it's in activity
            .load(imageList[position].input_image_hres_url)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.ivProcessed)
    }

    override fun getItemCount() = imageList.size
}