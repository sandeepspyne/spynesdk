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
import com.spyneai.databinding.ItemReshootEcomBinding
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.data.OnOverlaySelectionListener

class ReshootEcomHolder(
    itemView: View,
    listener: OnItemClickListener?,
    overlaySelectionListener : OnOverlaySelectionListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<Image>{

    var listener: OnItemClickListener? = null
    var overlaySelectionListener: OnOverlaySelectionListener? = null
    var binding : ItemReshootEcomBinding? = null
    val TAG = "ReshootEcomHolder"

    init {
        binding = ItemReshootEcomBinding.bind(itemView)
        this.listener = listener
        this.overlaySelectionListener = overlaySelectionListener
    }

    override fun bind(data: Image) {
        when{
            data.isSelected -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(
                    BaseApplication.getContext(),
                    R.drawable.bg_overlay_selected)
                overlaySelectionListener?.onOverlaySelected(
                    binding?.flOverlay!!,
                    adapterPosition,
                    data
                )
            }

            data.imageClicked -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(
                    BaseApplication.getContext(),
                    R.drawable.bg_overlay_image_clicked)
            }

            else -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(
                    BaseApplication.getContext(),
                    R.drawable.bg_overlay)
            }
        }

        if (data.imageClicked){
            Glide.with(itemView)
                .load(data.imagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding?.ivClicked!!)
        }else {
            Glide.with(itemView)
                .load(data.output_image_lres_url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding?.ivClicked!!)
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