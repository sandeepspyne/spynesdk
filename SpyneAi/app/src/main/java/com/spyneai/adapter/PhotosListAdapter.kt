package com.spyneai.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.model.order.Photo

class PhotosListAdapter(val context: Context,
                         val photoList: List<Photo>,
                         val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<PhotosListAdapter.ViewHolder>() {

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
                .inflate(R.layout.row_photos_list, viewGroup, false)

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
