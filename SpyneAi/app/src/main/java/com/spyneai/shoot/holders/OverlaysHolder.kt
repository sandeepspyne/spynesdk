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
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.databinding.ItemOverlaysBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.OnOverlaySelectionListener

class OverlaysHolder(
    itemView: View,
    listener: OnItemClickListener?,
    overlaySelectionListener : OnOverlaySelectionListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<OverlaysResponse.Data>{

    var listener: OnItemClickListener? = null
    var overlaySelectionListener: OnOverlaySelectionListener? = null
    var binding : ItemOverlaysBinding? = null

    val TAG = "OverlaysHolder"

    init {
        binding = ItemOverlaysBinding.bind(itemView)
        this.listener = listener
        this.overlaySelectionListener = overlaySelectionListener
    }

    override fun bind(data: OverlaysResponse.Data) {
        //set sequence number as per adapter position
        data.sequenceNumber = adapterPosition

        binding.apply {
            this?.tvName?.text = data.display_name
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
            if (!data.imagePath.contains("http")
                &&
                (data.prod_cat_id != AppConstants.CARS_CATEGORY_ID &&
                        data.prod_cat_id != AppConstants.BIKES_CATEGORY_ID)){
                binding?.ivOverlay?.rotation = 90f
            }

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
            listener?.onItemClick(
                it,
                adapterPosition,
                data
            )
        }
    }
}