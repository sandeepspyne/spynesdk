package com.spyneai.dashboard.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.dashboard.data.model.SliderModel

class SliderAdapter(
    val context: Context,
    val sliderImageList: ArrayList<SliderModel>,
//    val btnlistener: BtnClickListener
) : RecyclerView.Adapter<SliderAdapter.ViewHolder>() {

    companion object {
        var mClickListener: CategoriesDashboardAdapter.BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_dashboard_slider, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        mySlider.setBeforeImage(sliderImageList[position]).setAfterImage(imgUrl2)



    }
    override fun getItemCount() = sliderImageList.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

}


