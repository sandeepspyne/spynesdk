package com.spyneai.orders.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel

class MyOngoingProjectAdapter(
    val context: Context,
    val getProjectList: List<GetProjectsResponse.Project_data>,
    val viewModel: MyOrdersViewModel
) : RecyclerView.Adapter<MyOngoingProjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyOngoingProjectAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing_projects, parent, false)
        return MyOngoingProjectAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyOngoingProjectAdapter.ViewHolder, position: Int) {

        if (getProjectList[position].sub_category == "360_exterior" || getProjectList[position].sub_category.equals(
                "360_interior"
            )
        ) {
            holder.llThreeSixty.visibility = View.VISIBLE
            holder.tvCategory.text = "Automobiles"
        } else {
            holder.tvCategory.text = getProjectList[position].category
        }

        if (getProjectList[position].category.equals("cat_d8R14zUNE") || getProjectList[position].category.equals("Automobiles"))
            holder.tvImageCount.visibility = View.VISIBLE


        try {
            Glide.with(context) // replace with 'this' if it's in activity
                .load(getProjectList[position].sku[0].images[0].input_lres)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(holder.ivThumbnail)
        } catch (e: Exception) {

        }

        if (getProjectList[position].status.equals("Uploaded")) {
            holder.tvImageCount.visibility = View.INVISIBLE
            holder.lottieProgressCircle.visibility = View.INVISIBLE
            holder.llUploaded.visibility = View.VISIBLE
        }

        holder.tvProjectName.text = getProjectList[position].project_name
        holder.tvSkus.text = getProjectList[position].total_sku.toString()
        holder.tvDate.text = getProjectList[position].created_on
        holder.tvImages.text = getProjectList[position].total_images.toString()

        holder.tvImageCount.text =
            getProjectList[position].processed_images.toString() + "/" + getProjectList[position].total_images.toString()

    }

    override fun getItemCount(): Int {
        return getProjectList.size
    }
}