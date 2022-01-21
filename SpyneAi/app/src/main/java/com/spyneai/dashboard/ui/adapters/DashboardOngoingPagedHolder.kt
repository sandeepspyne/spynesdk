package com.spyneai.dashboard.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.getFormattedDate
import com.spyneai.orders.data.paging.OngoingPagedHolder
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.toDate

class DashboardOngoingPagedHolder(
    val context: Context,
    val view: View
) : RecyclerView.ViewHolder(view) {

    val ivImage: ImageView = view.findViewById(R.id.ivImage)
    val tvProject: TextView = view.findViewById(R.id.tvProject)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val clBackground: ConstraintLayout = view.findViewById(R.id.clBackground)
    val llFailed: LinearLayout = view.findViewById(R.id.llFailed)
    val llOngoing: LinearLayout = view.findViewById(R.id.llOngoing)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(
            context: Context,
            parent: ViewGroup
        ): DashboardOngoingPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.rv_ongoing_dashboard, parent, false)
            return DashboardOngoingPagedHolder(context, view)
        }
    }

    fun bind(item: Project?) {
        item?.let { showData(it) }
    }

    private fun showData(item: Project) {
        tvProject.text = item.projectName
        try {
            tvDate.text = getFormattedDate(item.createdOn)
        }catch (e : java.lang.Exception){

        }

        try {
            if (item.imagesCount == 0) {
                if (item.subCategoryName == "360_exterior"
                    || item.subCategoryName.equals("360_interior")){
                    Glide.with(context)
                        .load(R.drawable.three_sixty_thumbnail)
                        .into(ivImage)
                }else {
                    Glide.with(context)
                        .load(R.mipmap.defaults)
                        .into(ivImage)
                }
            }else {
                if (item.thumbnail == null){
                    if (item.subCategoryName == "360_exterior"
                        || item.subCategoryName.equals("360_interior")){
                        Glide.with(context)
                            .load(R.drawable.three_sixty_thumbnail)
                            .into(ivImage)
                    }else {
                        Glide.with(context)
                            .load(R.mipmap.defaults)
                            .into(ivImage)
                    }
                }else{
                    Glide.with(context) // replace with 'this' if it's in activity
                        .load(item.thumbnail)
                        .into(ivImage)
                }

            }
        }catch (e : Exception){
            e.printStackTrace()
        }catch (e : IndexOutOfBoundsException){
            e.printStackTrace()
        }

        clBackground.setOnClickListener {
            val intent = Intent(context, MyOrdersActivity::class.java)
            intent.putExtra("TAB_ID", 1)
            context.startActivity(intent)
        }

    }
}