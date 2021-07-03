package com.spyneai

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemOngoingTestBinding

class OngoingTestHolder(
    val view: View,
    val listener: OnItemClickListener?,
    var binding: ItemOngoingTestBinding? = null
) : RecyclerView.ViewHolder(view),GenericAdapter.Binder<OngoingTest> {

    init {
        binding = ItemOngoingTestBinding.bind(view)
    }

    override fun bind(data: OngoingTest) {
        binding.apply {
            this?.tvDesignation?.text = data.designation
            binding?.tvName?.text = data.name
            binding?.tvDesignation?.setOnClickListener {
                listener?.onItemClick(
                    it,
                    adapterPosition,
                    data
                )
            }
        }
    }
}