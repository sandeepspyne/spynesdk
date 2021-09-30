package com.spyneai

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.GenericAdapter
import com.spyneai.base.JavaViewHolderFactory
import com.spyneai.base.OnItemClickListener

class OngoingTestAdapter(
    list : List<Any>,
    var listener : OnItemClickListener
    ) : GenericAdapter<Any>(list) {

    override fun getLayoutId(position: Int, obj: Any?): Int {
        return when (obj) {
            is OngoingTest -> R.layout.item_ongoing_test
            else -> error("Unknown type: for position: $position")
        }
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return JavaViewHolderFactory.create(view, viewType, listener)
    }
}