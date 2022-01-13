package com.spyneai.orders.data.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.spyneai.R
import com.spyneai.shoot.repository.model.project.Project

class CompletedPagedHolder(view: View) : RecyclerView.ViewHolder(view) {

    val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
    val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    val tvSkus: TextView = view.findViewById(R.id.tvSkus)
    val llThreeSixty: LinearLayout = view.findViewById(R.id.llThreeSixty)
    val tvImages: TextView = view.findViewById(R.id.tvImages)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val tvPaid: TextView = view.findViewById(R.id.tvPaid)
    val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    val ivDownloadSKU: ImageView = view.findViewById(R.id.ivDownloadSKU)
    val cvMain: CardView = view.findViewById(R.id.cvMain)
    val flCategory: FrameLayout = view.findViewById(R.id.flCategory)
    val flSkus: FrameLayout = view.findViewById(R.id.flSkus)
    val flImages: FrameLayout = view.findViewById(R.id.flImages)
    val tvCategories: TextView = view.findViewById(R.id.tvCategories)
    val tvSku: TextView = view.findViewById(R.id.tvSku)
    val tvImage: TextView = view.findViewById(R.id.tvImage)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(parent: ViewGroup): CompletedPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_completed_projects, parent, false)
            return CompletedPagedHolder(view)
        }
    }

    fun bind(item: Project?) {
        item?.let {
            showData(item)
        }
    }

    private fun showData(item: Project) {
        tvProjectName.text = item?.projectName
        tvCategory.text = item?.categoryName
        tvDate.text = item?.createdOn
        tvSkus.text = item?.skuCount.toString()
    }
}