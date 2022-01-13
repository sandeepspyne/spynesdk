package com.spyneai.draft.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.orders.data.paging.OngoingPagedHolder
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku

class SkuPagedHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {

    val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
    val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    val tvImages: TextView = view.findViewById(R.id.tvImages)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    val cvMain: CardView = view.findViewById(R.id.cvMain)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            parent: ViewGroup
        ): SkuPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_draft_sku, parent, false)
            return SkuPagedHolder(context, view)
        }
    }

    fun bind(item: Sku?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Sku) {
        tvSkuName.text = item?.skuName
        tvCategory.text = item?.categoryName
        tvDate.text = item?.createdAt.toString()
        tvImages.text = item?.imagesCount.toString()
    }
}