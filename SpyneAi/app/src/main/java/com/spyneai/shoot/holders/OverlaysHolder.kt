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
import com.spyneai.databinding.ItemOverlaysBinding
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.OnOverlaySelectionListener

class OverlaysHolder(
    itemView: View,
    listener: OnItemClickListener?,
    overlaySelectionListener : OnOverlaySelectionListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<OverlaysResponse.Overlays>{

    var listener: OnItemClickListener? = null
    var overlaySelectionListener: OnOverlaySelectionListener? = null
    var binding : ItemOverlaysBinding? = null

    val TAG = "OverlaysHolder"

    init {
        binding = ItemOverlaysBinding.bind(itemView)
        this.listener = listener
        this.overlaySelectionListener = overlaySelectionListener
    }

    override fun bind(overlays: OverlaysResponse.Overlays) {
        //set sequence number as per adapter position
        overlays.sequenceNumber = adapterPosition

        binding.apply {
            this?.tvName?.text = overlays.display_name
        }

        when{
            overlays.isSelected -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(BaseApplication.getContext(),R.drawable.bg_overlay_selected)
                overlaySelectionListener?.onOverlaySelected(
                    binding?.flOverlay!!,
                    adapterPosition,
                    overlays
                )
            }

            overlays.imageClicked -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(BaseApplication.getContext(),R.drawable.bg_overlay_image_clicked)
            }

            else -> {
                binding?.flOverlay?.background = ContextCompat.getDrawable(BaseApplication.getContext(),R.drawable.bg_overlay)
            }
        }

        if (overlays.imageClicked){
            if (!overlays.imagePath!!.contains("http")
                &&
                (overlays.prod_cat_id != AppConstants.CARS_CATEGORY_ID &&
                        overlays.prod_cat_id != AppConstants.BIKES_CATEGORY_ID)){
                itemView.loadSmartly(overlays.display_thumbnail,binding?.ivOverlay!!)
            }else{
                Glide.with(itemView)
                    .load(overlays.imagePath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding?.ivOverlay!!)
            }
        }else {
            Glide.with(itemView)
                .load(overlays.display_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding?.ivOverlay!!)
        }

        binding?.flOverlay?.setOnClickListener {
            listener?.onItemClick(
                it,
                adapterPosition,
                overlays
            )
        }
    }
}