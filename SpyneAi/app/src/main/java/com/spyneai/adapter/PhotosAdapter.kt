package com.spyneai.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.CameraActivity
import com.spyneai.activity.CameraPreviewActivity
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.channel.Data
import com.spyneai.model.order.Photo
import com.spyneai.model.order.Sku
import com.spyneai.model.sku.Photos
import com.spyneai.needs.AppConstants

 class PhotosAdapter(val context: Context,
                     val photoList: List<Photos>,
                     val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {

     companion object {
         var mClickListener: BtnClickListener? = null
     }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPhotos: ImageView = view.findViewById(R.id.imgPhotos)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_photos, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Glide.with(context).load(photoList[position].displayThumbnail).into(viewHolder.imgPhotos)

        mClickListener = btnlistener
        viewHolder.imgPhotos.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = photoList.size
     open interface BtnClickListener {
         fun onBtnClick(position: Int)
     }
}
