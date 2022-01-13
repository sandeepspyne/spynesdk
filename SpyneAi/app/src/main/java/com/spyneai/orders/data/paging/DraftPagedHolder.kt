package com.spyneai.orders.data.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.shoot.repository.model.project.Project

class DraftPagedHolder(view: View) : RecyclerView.ViewHolder(view) {

    val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
    val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    val tvSkus: TextView = view.findViewById(R.id.tvSkus)
    val llThreeSixty: LinearLayout = view.findViewById(R.id.llThreeSixty)
    val tvImages: TextView = view.findViewById(R.id.tvImages)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val tvPaid: TextView = view.findViewById(R.id.tvPaid)
    val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
    val cvMain: CardView = view.findViewById(R.id.cvMain)

    companion object {
        //get instance of the DoggoImageViewHolder
        fun getInstance(parent: ViewGroup): DraftPagedHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_draft_project, parent, false)
            return DraftPagedHolder(view)
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