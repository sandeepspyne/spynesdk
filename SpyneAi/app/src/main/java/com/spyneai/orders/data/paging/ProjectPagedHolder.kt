package com.spyneai.orders.data.paging

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.orders.ui.activity.OngoingSkusActivity

class ProjectPagedHolder(view: View) : RecyclerView.ViewHolder(view) {

    val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
    val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    val tvSkus: TextView = view.findViewById(R.id.tvSkus)
    val llThreeSixty: LinearLayout = view.findViewById(R.id.llThreeSixty)
    val tvImages: TextView = view.findViewById(R.id.tvImages)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    val tvImageCount: TextView = view.findViewById(R.id.tvImageCount)
    val lottieProgressCircle: LottieAnimationView = view.findViewById(R.id.lottieProgressCircle)
    val llUploaded: LinearLayout = view.findViewById(R.id.llUploaded)
    val cvMain: CardView = view.findViewById(R.id.cvMain)
    val flAnimationContainer: FrameLayout = view.findViewById(R.id.flAnimationContainer)
    val flCategory: FrameLayout = view.findViewById(R.id.flCategory)
    val flSkus: FrameLayout = view.findViewById(R.id.flSkus)
    val flImages: FrameLayout = view.findViewById(R.id.flImages)
    val tvCategories: TextView = view.findViewById(R.id.tvCategories)
    val tvSku: TextView = view.findViewById(R.id.tvSku)
    val tvImage: TextView = view.findViewById(R.id.tvImage)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(parent: ViewGroup): ProjectPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_ongoing_projects, parent, false)
            return ProjectPagedHolder(view)
        }
    }

    fun bind(item: ProjectPagedRes.ProjectPagedResItem?) {
        showData(item)
    }

    private fun showData(item: ProjectPagedRes.ProjectPagedResItem?) {
        tvProjectName.text = item?.projectName
        tvCategory.text = item?.category
        tvDate.text = item?.createdOn
    }
}