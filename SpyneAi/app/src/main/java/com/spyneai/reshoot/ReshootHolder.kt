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
import com.spyneai.reshoot.data.ReshootOverlaysRes
import com.spyneai.shoot.data.OnOverlaySelectionListener

class ReshootHolder(
    itemView: View,
    listener: OnItemClickListener?,
    overlaySelectionListener : OnOverlaySelectionListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<ReshootOverlaysRes.Data>{

    var listener: OnItemClickListener? = null
    var overlaySelectionListener: OnOverlaySelectionListener? = null
    var binding : ItemReshootBinding? = null
    val TAG = "OverlaysHolder"

    init {
        binding = ItemReshootBinding.bind(itemView)
        this.listener = listener
        this.overlaySelectionListener = overlaySelectionListener
    }

    override fun bind(data: ReshootOverlaysRes.Data) {
        binding.apply {
            this?.tvName?.text = data.displayName
        }

        when{
            data.isSelected -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(BaseApplication.getContext(),R.drawable.bg_overlay_selected)
                overlaySelectionListener?.onOverlaySelected(
                    binding?.flOverlay!!,
                    adapterPosition,
                    data
                )
            }

            data.imageClicked -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(BaseApplication.getContext(),R.drawable.bg_overlay_image_clicked)
            }

            else -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(BaseApplication.getContext(),R.drawable.bg_overlay)
            }
        }

        if (data.imageClicked){
            Glide.with(itemView)
                .load(data.imagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding?.ivOverlay!!)
        }
        else {
            Glide.with(itemView)
                .load(data.displayThumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding?.ivOverlay!!)
        }

        binding?.flOverlay?.setOnClickListener {
            listener?.onItemClick(
                it,
                adapterPosition,
                data
            )
        }
    }
}