package com.spyneai.adapter

import android.annotation.SuppressLint
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
import com.spyneai.model.channel.ChannelsResponse
import java.util.*

class MarketAdapter(
        val context: Context,
        val channelList : ArrayList<ChannelsResponse>,
        val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<MarketAdapter.ViewHolder>() {

    // true if the user in selection mode, false otherwise
    private var multiSelect = true
    // Keeps track of all the selected images
    private val selectedItems = ArrayList<ChannelsResponse>()

     companion object {
         var mClickListener: BtnClickListener? = null
     }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardMarket: CardView = view.findViewById(R.id.cardMarket)
        val imgPhotos: ImageView = view.findViewById(R.id.imgPhotos)
        val tvMarketPlace: TextView = view.findViewById(R.id.tvMarketPlace)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_markets, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        //MultiSelect Android
        val currentItem = channelList[position]
        if (selectedItems.contains(currentItem)) {
            // if the item is selected, let the user know by adding a dark layer above it
            viewHolder.cardMarket.setBackgroundResource(R.drawable.bg_selected);
        } else {
            // else, keep it as it is
            viewHolder.cardMarket.setBackgroundResource(R.drawable.bg_channel);
        }

        //Set multiselect


        Glide.with(context).load(channelList[position].image_url)
            .into(viewHolder.imgPhotos)

        viewHolder.tvMarketPlace.setText(channelList[position].category)
        mClickListener = btnlistener

        viewHolder.cardMarket.setOnClickListener(View.OnClickListener {
            if (multiSelect)
                selectItem(viewHolder, currentItem)
            if (mClickListener != null)
                mClickListener?.onBtnClick(position,selectedItems)

        })

    }


    // helper function that adds/removes an item to the list depending on the app's state
    private fun selectItem(viewHolder: ViewHolder, currentItem: ChannelsResponse) {
        // If the "selectedItems" list contains the item, remove it and set it's state to normal
        if (selectedItems.contains(currentItem)) {
            selectedItems.remove(currentItem)
            viewHolder.cardMarket.setBackgroundResource(R.drawable.bg_channel);
        } else {
            // Else, add it to the list and add a darker shade over the image, letting the user know that it was selected
            selectedItems.add(currentItem)
            viewHolder.cardMarket.setBackgroundResource(R.drawable.bg_selected);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = channelList.size
     open interface BtnClickListener {
         fun onBtnClick(position: Int, selectedItems: ArrayList<ChannelsResponse>)
     }
}
