package com.spyneai.shoot.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse

class OverlaysAdapter (
    list: List<Any>,
    var listener: OnItemClickListener
) : GenericAdapter<Any>(list) {

    override fun getLayoutId(position: Int, obj: Any?): Int {
        return when (obj) {

            is OverlaysResponse.Data -> R.layout.item_overlays
            is NewSubCatResponse.Interior -> R.layout.item_interior
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return JavaViewHolderFactory.create(view, viewType, listener)
    }


}