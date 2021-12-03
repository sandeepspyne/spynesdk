package com.spyneai.shoot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import java.util.*

class SkuImageAdapter(
    val context: Context, private var shootList: ArrayList<ShootData>
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
        viewHolder.ivCapturedImage.setRotation(90F)

        Glide.with(context).load(
            shootList[position].capturedImage)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(viewHolder.ivCapturedImage)
    }


    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount() = if (shootList == null) 0 else shootList.size


}