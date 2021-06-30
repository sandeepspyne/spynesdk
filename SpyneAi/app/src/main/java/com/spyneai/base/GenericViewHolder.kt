package com.spyneai.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GenericViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    GenericAdapter.Binder<Any> {
    override fun bind(data: Any) {
    }
}