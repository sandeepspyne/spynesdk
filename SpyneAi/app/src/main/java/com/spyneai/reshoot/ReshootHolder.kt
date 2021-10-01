package com.spyneai.reshoot

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.databinding.ItemOverlaysBinding
import com.spyneai.databinding.ItemReshootBinding
import com.spyneai.orders.data.response.ImagesOfSkuRes

class ReshootHolder(
    itemView: View,
    listener: OnItemClickListener?)
    : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<ImagesOfSkuRes.Data>{

    var listener: OnItemClickListener? = null
    var binding : ItemReshootBinding? = null
    val TAG = "OverlaysHolder"

    init {
        binding = ItemReshootBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: ImagesOfSkuRes.Data) {

        Glide.with(itemView)
            .load(data.input_image_hres_url)
            .into(binding?.ivBefore!!)

        Glide.with(itemView)
            .load(data.output_image_hres_url)
            .into(binding?.ivAfter!!)

    }
}