package com.spyneai.reshoot

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.BaseApplication
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemReshootBinding
import com.spyneai.orders.data.response.ImagesOfSkuRes

class ReshootHolder(
    itemView: View,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<ImagesOfSkuRes.Data>{

    var listener: OnItemClickListener? = null
    var binding : ItemReshootBinding? = null
    val TAG = "OverlaysHolder"

    init {
        binding = ItemReshootBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: ImagesOfSkuRes.Data) {
        //set sequence number as per adapter position
//        data.sequenceNumber = adapterPosition
//
//        binding.apply {
//            this?.tvName?.text = data.display_name
//        }
//
        if (data.isSelected)
            binding?.flOverlay?.background = ContextCompat.getDrawable(
                BaseApplication.getContext(),
                R.drawable.bg_overlay_selected)
        else
            binding?.flOverlay?.background = ContextCompat.getDrawable(
                BaseApplication.getContext(),
                R.drawable.bg_overlay)

        if (data.imageClicked){
            Glide.with(itemView)
                .load(data.imagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding?.ivOverlay!!)
        }
//        else {
//            Glide.with(itemView)
//                .load(data.display_thumbnail)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(binding?.ivOverlay!!)
//        }

        binding?.flOverlay?.setOnClickListener {
            listener?.onItemClick(
                it,
                adapterPosition,
                data
            )
        }
    }
}