package com.spyneai.shoot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.ShootViewModel
import java.util.ArrayList

class CapturedImageAdapter(
    val context: Context, private var shootList: ArrayList<String>
)
: RecyclerView.Adapter<CapturedImageAdapter.ViewHolder>() {

    private val viewModel = ShootViewModel()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCapturedImage: ImageView = view.findViewById(R.id.ivCapturedImage)
        val tvImageCount: TextView = view.findViewById(R.id.tvImageCount)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_captured_image, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvImageCount.text = (position + 1).toString()
        Glide.with(context).load(
                    shootList[position])
            .into(viewHolder.ivCapturedImage)
    }


    fun removeLastItem() {
        if (shootList.isNullOrEmpty()) return
        shootList.removeAt(shootList.size - 1)
        notifyDataSetChanged()
    }

    // Return the size of your data set (invoked by the layout manager)
    override fun getItemCount() = if (shootList == null) 0 else shootList.size


}