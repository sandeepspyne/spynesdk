package com.spyneai.orders.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R

class MyCompletedOrdersAdapter (val context: Context)  : RecyclerView.Adapter<MyCompletedOrdersAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail : ImageView =  view.findViewById(R.id.iv_thumbnail)
        val cvDownload : CardView =  view.findViewById(R.id.cv_download)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCompletedOrdersAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_completed_orders, parent, false)

        return MyCompletedOrdersAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCompletedOrdersAdapter.ViewHolder, position: Int) {

//        holder.ivThumbnail.setOnClickListener{ view ->
//            view.findNavController().navigate(R.id.nav_market_details)
//        }

        holder.cvDownload.setOnClickListener{ view ->
            view.findNavController().navigate(R.id.nav_request_download)
        }
    }

    override fun getItemCount(): Int {
        return 10
    }
}