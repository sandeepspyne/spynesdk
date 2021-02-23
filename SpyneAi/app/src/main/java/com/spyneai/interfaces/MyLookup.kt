package com.spyneai.interfaces

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_order.*

class MyLookup(private val rv: RecyclerView)
    : ItemDetailsLookup<String>() {
    override fun getItemDetails(event: MotionEvent)
            : ItemDetails<String>? {

      /*  val view = rvChannels.findChildViewUnder(event.x, event.y)
        if(view != null) {
            return (rvChannels.getChildViewHolder(view) as MyViewHolder)
                .getItemDetails()
        }*/
        return null
    }
}