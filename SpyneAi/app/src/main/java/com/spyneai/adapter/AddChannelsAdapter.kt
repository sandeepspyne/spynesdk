package com.spyneai.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.CameraActivity
import com.spyneai.activity.CameraPreviewActivity
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.channel.Data
import com.spyneai.model.order.MarketPlace
import com.spyneai.needs.AppConstants

 class AddChannelsAdapter(val context: Context,
                          val channelList : ArrayList<MarketPlace>,
                          val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<AddChannelsAdapter.ViewHolder>() {

     companion object {
         var mClickListener: BtnClickListener? = null
     }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llChannel: LinearLayout = view.findViewById(R.id.llChannel)
        val ivMarketPlace: ImageView = view.findViewById(R.id.ivMarketPlace)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_channels_add, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Glide.with(context).load(AppConstants.BASE_IMAGE_URL +
                channelList[position].displayThumbnail)
                .into(viewHolder.ivMarketPlace)

        mClickListener = btnlistener
        viewHolder.llChannel.setOnClickListener(View.OnClickListener {
            if (ChannelsAdapter.mClickListener != null)
                ChannelsAdapter.mClickListener?.onBtnClick(position)
        })

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = channelList.size
     open interface BtnClickListener {
         fun onBtnClick(position: Int)
     }
}
