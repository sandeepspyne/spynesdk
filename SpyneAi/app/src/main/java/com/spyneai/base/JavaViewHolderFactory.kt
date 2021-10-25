package com.spyneai.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.reshoot.ReshootHolder
import com.spyneai.reshoot.SelectImageHolder
import com.spyneai.shoot.data.OnOverlaySelectionListener
import com.spyneai.shoot.holders.*


object JavaViewHolderFactory {

    fun create(view: View, viewType: Int, listener: OnItemClickListener,
                overlaySelectionListener: OnOverlaySelectionListener? = null): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_subcategories -> SubcategoryHolder(view, listener)
            R.layout.item_overlays -> OverlaysHolder(view, listener,overlaySelectionListener)
            R.layout.item_interior -> InteriorHolder(view, listener,overlaySelectionListener)
            R.layout.item_miscellanous -> MiscHolder(view, listener,overlaySelectionListener)
            R.layout.item_select_image -> SelectImageHolder(view, listener)
            R.layout.item_reshoot -> ReshootHolder(view,listener,overlaySelectionListener)
            R.layout.item_clicked -> ClickedHolder(view,listener,overlaySelectionListener)

            else -> GenericViewHolder(view)
        }
    }


}