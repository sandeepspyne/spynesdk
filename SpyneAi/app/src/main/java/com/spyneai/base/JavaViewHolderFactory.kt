package com.spyneai.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.OngoingTestHolder
import com.spyneai.R
import com.spyneai.reshoot.ReshootHolder
import com.spyneai.shoot.holders.OverlaysHolder
import com.spyneai.shoot.holders.SubcategoryHolder


object JavaViewHolderFactory {

    fun create(view: View, viewType: Int, listener: OnItemClickListener): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_ongoing_test -> OngoingTestHolder(view, listener)
            R.layout.item_subcategories -> SubcategoryHolder(view, listener)
            R.layout.item_overlays -> OverlaysHolder(view, listener)
            R.layout.item_reshoot -> ReshootHolder(view, listener)

            else -> GenericViewHolder(view)
        }
    }


}