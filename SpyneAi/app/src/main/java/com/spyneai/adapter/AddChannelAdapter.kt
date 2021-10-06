package com.spyneai.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.model.order.MarketPlace
import com.spyneai.needs.AppConstants

class AddChannelAdapter(val context: Context,
                        val channelList : ArrayList<MarketPlace>,
                        val btnlistener: AddChannelAdapter.BtnClickListener?)
    : RecyclerView.Adapter<AddChannelAdapter.ViewHolder>() {

    // private var tracker: SelectionTracker<Long>? = null
    companion object {
        var mClickListener: BtnClickListener? = null
    }

/*
     fun setTracker(tracker: SelectionTracker<Long>?) {
         this.tracker = tracker
     }
*/
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardChannel: CardView = view.findViewById(R.id.cardChannel)
        val imgSku: ImageView = view.findViewById(R.id.imgSku)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_channel_add, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        /*  if (channelList[position].displayThumbnail.toString().contains("clippr"))
              Glide.with(context).load(channelList[position].displayThumbnail).into(viewHolder.imgSku)
          else*/
        Glide.with(context).load(AppConstants.BASE_IMAGE_URL +
                channelList[position].displayThumbnail)
                .into(viewHolder.imgSku)

        Log.e("Image",channelList[position].displayThumbnail.toString())
        mClickListener = btnlistener
        viewHolder.cardChannel.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)

        })

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = channelList.size
    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }
}
