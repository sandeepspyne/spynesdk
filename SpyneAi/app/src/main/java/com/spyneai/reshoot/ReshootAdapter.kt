package com.spyneai.reshoot

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.reshoot.data.ReshootOverlaysRes
import com.spyneai.shoot.data.OnOverlaySelectionListener

class ReshootAdapter(
    list: List<Any>,
    var listener: OnItemClickListener,
    var overlaySelectionListener : OnOverlaySelectionListener
) : GenericAdapter<Any>(list) {

    override fun getLayoutId(position: Int, obj: Any?): Int {
        return when (obj) {

            is ReshootOverlaysRes.Data -> R.layout.item_reshoot
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return JavaViewHolderFactory.create(view, viewType, listener,overlaySelectionListener)
    }


}