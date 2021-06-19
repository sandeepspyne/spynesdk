package com.spyneai.orders.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R

class MyOngoingOrdersAdapter (val context: Context)  : RecyclerView.Adapter<MyOngoingOrdersAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyOngoingOrdersAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_ongoing_orders, parent, false)
        return MyOngoingOrdersAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyOngoingOrdersAdapter.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 5
    }
}