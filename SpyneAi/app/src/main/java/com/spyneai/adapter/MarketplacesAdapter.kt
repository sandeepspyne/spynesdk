package com.spyneai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.marketplace.FootwearMarketplaceResponse

class MarketplacesAdapter(val context: Context,
                          val channelList : ArrayList<FootwearMarketplaceResponse>,
                          var pos : Int,
                          val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<MarketplacesAdapter.ViewHolder>() {

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
        val tvMarketplacesName: TextView = view.findViewById(R.id.tvMarketplacesName)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.marketplaces_channel, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Glide.with(context).load(channelList[position].image_url)
                .into(viewHolder.ivMarketPlace)

        viewHolder.tvMarketplacesName.setText(channelList[position].marketplace_name)

        mClickListener = btnlistener
/*
        viewHolder.llNoBg.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(-1)
            pos = -1

            viewHolder.llChannel.setBackgroundResource(R.drawable.bg_selected)
        })
*/


        if (position == pos)
            viewHolder.llChannel.setBackgroundResource(R.drawable.bg_selected)
        else
            viewHolder.llChannel.setBackgroundResource(R.drawable.bg_channel)

        viewHolder.llChannel.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)

            pos = position

            viewHolder.llChannel.setBackgroundResource(R.drawable.bg_selected)
        })

/*
        if (position != -1)
            if (position == pos)
                viewHolder.llChannel.setBackgroundResource(R.drawable.bg_selected)
            else
                viewHolder.llChannel.setBackgroundResource(R.drawable.bg_channel)
*/

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = channelList.size
    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }
}
