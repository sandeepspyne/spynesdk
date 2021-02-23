package com.spyneai.adapter

import android.annotation.SuppressLint
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
import com.spyneai.model.orders.Data
import com.spyneai.model.orders.Ongoing
import com.spyneai.model.orders.Submitted
import com.spyneai.needs.AppConstants

class MyOrdersSubmittedAdapter(val context: Context,
                               val myOrdersSubmittedResponse : ArrayList<Submitted>,
                               val btnlistener: BtnClickListener)
    : RecyclerView.Adapter<MyOrdersSubmittedAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvShootName: TextView = view.findViewById(R.id.tvShootName)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val llCompleted: LinearLayout = view.findViewById(R.id.llCompleted)
        val ivCompleted: ImageView = view.findViewById(R.id.ivCompleted)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_my_orders, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.tvShootName.setText(myOrdersSubmittedResponse[position].shootName)
        viewHolder.tvCategoryName.setText("Category : " + myOrdersSubmittedResponse[position].categoryName)
        viewHolder.tvProductName.setText("Product : " + myOrdersSubmittedResponse[position].productName)

        Glide.with(context).load(
                AppConstants.BASE_IMAGE_URL + myOrdersSubmittedResponse[position].skuOneDisplayThumnail)
            .placeholder(R.mipmap.defaults).into(viewHolder.ivCompleted)

        if(!myOrdersSubmittedResponse[position].skuTwoDisplayThumnail.isEmpty() )
            Glide.with(context).load(myOrdersSubmittedResponse[position].skuTwoDisplayThumnail).into(viewHolder.ivCompleted)

        mClickListener = btnlistener

        viewHolder.llCompleted.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myOrdersSubmittedResponse.size
    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

}
