package com.spyneai.shoot.holders

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.BaseApplication
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.databinding.ItemOverlaysBinding
import com.spyneai.databinding.ItemSubcategoriesBinding
import com.spyneai.needs.AppConstants

class OverlaysHolder(
    itemView: View,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<OverlaysResponse.Data>{

    var listener: OnItemClickListener? = null
    var binding : ItemOverlaysBinding? = null

    init {
        binding = ItemOverlaysBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(data: OverlaysResponse.Data) {
        binding.apply {
            this?.tvName?.text = data.angle_name
        }

        if (data.isSelected)
            binding?.flOverlay?.background = ContextCompat.getDrawable(BaseApplication.getContext(),R.drawable.bg_overlay_selected)
        else
            binding?.flOverlay?.background = ContextCompat.getDrawable(BaseApplication.getContext(),R.drawable.bg_overlay)

        if (data.imageClicked){
            Glide.with(itemView)
                .load(data.imagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding?.ivOverlay!!)
        }else {
            Glide.with(itemView)
                .load(data.display_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding?.ivOverlay!!)
        }

        binding?.flOverlay?.setOnClickListener {
            val s = ""
            listener?.onItemClick(
                it,
                adapterPosition,
                data
            )
        }
    }
}