package com.spyneai.shoot.holders

import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.BaseApplication
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemClickedBinding
import com.spyneai.shoot.data.OnOverlaySelectionListener
import com.spyneai.shoot.data.model.ShootData

class ClickedHolder(
    itemView: View,
    listener: OnItemClickListener?,
    overlaySelectionListener: OnOverlaySelectionListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<ShootData> {

    var listener: OnItemClickListener? = null
    var overlaySelectionListener: OnOverlaySelectionListener? = null
    var binding: ItemClickedBinding? = null

    val TAG = "OverlaysHolder"

    init {
        binding = ItemClickedBinding.bind(itemView)
        this.listener = listener
        this.overlaySelectionListener = overlaySelectionListener
    }

    override fun bind(data: ShootData) {
        //set overlay id as per adapter position
        data.overlayId = adapterPosition

        when {
            data.isSelected -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(
                    BaseApplication.getContext(),
                    R.drawable.bg_overlay_selected
                )
                overlaySelectionListener?.onOverlaySelected(
                    binding?.flOverlay!!,
                    adapterPosition,
                    data
                )
            }

            data.imageClicked -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(
                    BaseApplication.getContext(),
                    R.drawable.bg_overlay_image_clicked
                )
            }

            else -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(
                    BaseApplication.getContext(),
                    R.drawable.bg_overlay
                )
            }
        }

        if (data.imageClicked) {
            if (!data.imagePath.contains("http"))
//                binding?.ivClicked?.rotation = 90f

            Glide.with(itemView)
                .load(data.capturedImage)
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