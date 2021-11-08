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
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.databinding.ItemInteriorBinding
import com.spyneai.databinding.ItemOverlaysBinding
import com.spyneai.service.log
import com.spyneai.shoot.data.OnOverlaySelectionListener

class InteriorHolder(
    itemView: View,
    listener: OnItemClickListener?,
    overlaySelectionListener : OnOverlaySelectionListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<NewSubCatResponse.Interior>{

    var listener: OnItemClickListener? = null
    var overlaySelectionListener: OnOverlaySelectionListener? = null
    var binding : ItemInteriorBinding? = null
    val TAG = "InteriorHolder"

    init {
        binding = ItemInteriorBinding.bind(itemView)
        this.listener = listener
        this.overlaySelectionListener = overlaySelectionListener
    }

    override fun bind(data: NewSubCatResponse.Interior) {
        //set sequence number as per adapter position
       // data.sequenceNumber = adapterPosition
        val s = ""
        Log.d(TAG, "bind: "+adapterPosition)
        Log.d(TAG, "bind: "+data.isSelected)
        Log.d(TAG, "bind: "+data.overlayId)

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
            Log.d(TAG, "bind: "+data.imagePath)
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