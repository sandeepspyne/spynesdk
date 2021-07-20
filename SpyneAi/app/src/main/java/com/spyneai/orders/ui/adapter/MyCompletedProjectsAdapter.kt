package com.spyneai.orders.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.play.core.assetpacks.v
import com.spyneai.R
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel

class MyCompletedProjectsAdapter(
    val context: Context,
    val getProjectList: List<GetProjectsResponse.Project_data>,
    val viewModel: MyOrdersViewModel
) : RecyclerView.Adapter<MyCompletedProjectsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvSkus: TextView = view.findViewById(R.id.tvSkus)
        val tvImages: TextView = view.findViewById(R.id.tvImages)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvPaid: TextView = view.findViewById(R.id.tvPaid)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val cvMain: CardView = view.findViewById(R.id.cvMain)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCompletedProjectsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_projects, parent, false)
        return MyCompletedProjectsAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCompletedProjectsAdapter.ViewHolder, position: Int) {


        if (getProjectList[0].sku[0].images.size != 0)
        Glide.with(context) // replace with 'this' if it's in activity
            .load(getProjectList[position].sku[0].images[0].input_lres)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.ivThumbnail)

        holder.tvProjectName.text = getProjectList[position].project_name
        holder.tvSkus.text = getProjectList[position].total_sku.toString()
//        holder.tvImages.text = getProjectList[position].sku[position].total_images.toString()

        holder.cvMain.setOnClickListener {
            viewModel.position.value = position
                it.findNavController().navigate(R.id.action_nav_completed_projects_fragment_to_nav_completed_skus_fragment)
        }


    }

    override fun getItemCount(): Int {
        return getProjectList.size
    }
}