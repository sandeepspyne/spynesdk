package com.spyneai.shoot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.shoot.data.ShootViewModel
import java.util.ArrayList

class SkuImageAdapter (
    val context: Context, private var shootList: ArrayList<String>
)
    : RecyclerView.Adapter<SkuImageAdapter.ViewHolder>() {

    private val viewModel = ShootViewModel()
    private var hideButton = false

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCapturedImage: ImageView = view.findViewById(R.id.ivCapturedImage)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_sku_images, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Glide.with(context).load(
            shootList[position])
            .into(viewHolder.ivCapturedImage)
    }


    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount() = if (shootList == null) 0 else shootList.size


}