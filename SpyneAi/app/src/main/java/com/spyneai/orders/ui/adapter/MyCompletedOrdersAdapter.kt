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
import com.spyneai.orders.data.response.GetCompletedSKUsResponse

class MyCompletedOrdersAdapter(
    val context: Context,
    completedSkuList: ArrayList<GetCompletedSKUsResponse>
) : RecyclerView.Adapter<MyCompletedOrdersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.iv_thumbnail)
        val cvDownload: CardView = view.findViewById(R.id.cv_download)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCompletedOrdersAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_completed_orders, parent, false)

        return MyCompletedOrdersAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCompletedOrdersAdapter.ViewHolder, position: Int) {


        holder.cvDownload.setOnClickListener { view ->
            view.findNavController().navigate(R.id.nav_request_download)
        }
    }

    override fun getItemCount(): Int {
        return 10
    }
}