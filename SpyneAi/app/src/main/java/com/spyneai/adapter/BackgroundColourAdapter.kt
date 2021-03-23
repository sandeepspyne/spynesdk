package com.spyneai.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.model.channels.MarketplaceResponse
import com.spyneai.model.marketplace.FootwearMarketplaceResponse
import kotlinx.android.synthetic.main.background_colour.view.*

class BackgroundColourAdapter (val context: Context,
                               val backgroundColourList : ArrayList<FootwearMarketplaceResponse >,
                               var pos : Int,
                               val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<BackgroundColourAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val llBackgroundChannel: LinearLayout = view.findViewById(R.id.llBackgroundChannel)
        val ivBackground: ImageView = view.findViewById(R.id.ivBackground)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.background_colour, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element



        if (position == 0){
            Glide.with(context).load(backgroundColourList[position].custom_bg_url_1).into(viewHolder.ivBackground)
        }else if (position == 1){
            Glide.with(context).load(backgroundColourList[position].custom_bg_url_2).into(viewHolder.ivBackground)
        }

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
            viewHolder.ivBackground.apply {
                ivBackground.borderColor = Color.RED
            }
        else
            viewHolder.ivBackground.apply {
                ivBackground.borderColor = Color.WHITE
            }

        viewHolder.ivBackground.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)

            pos = position



            viewHolder.ivBackground.apply {
                ivBackground.borderColor = Color.RED
            }

//            viewHolder.ivBackground.setBackgroundResource(R.drawable.bg_selected)
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
    override fun getItemCount() = 2

}
