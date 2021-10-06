package com.spyneai.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.model.channel.BackgroundsResponse

class BackgroundsAdapter(val context: Context,
                          val backgroundList : ArrayList<BackgroundsResponse>,
                          val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<BackgroundsAdapter.ViewHolder>() {


     companion object {
         var mClickListener: BtnClickListener? = null
     }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llChannel: LinearLayout = view.findViewById(R.id.llChannel)
        val cardBackground: CardView = view.findViewById(R.id.cardBackground)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.background, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        if (!backgroundList[position].hex_code.equals("NA"))
            viewHolder.cardBackground.setBackgroundColor(Color.parseColor(backgroundList[position].hex_code))
        mClickListener = btnlistener

        viewHolder.cardBackground.setOnClickListener(View.OnClickListener {
            if (ChannelAdapter.mClickListener != null)
                ChannelAdapter.mClickListener?.onBtnClick(position)
        })

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = backgroundList.size
     open interface BtnClickListener {
         fun onBtnClick(position: Int)
     }

 }
