package com.spyneai.processedimages.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ItemProcessedImageBinding
import com.spyneai.shoot.repository.model.image.Image


class ProcessedImagesHolder(
    val view: View,
    val listener: OnItemClickListener?,
    var binding: ItemProcessedImageBinding? = null
) : RecyclerView.ViewHolder(view), GenericAdapter.Binder<Image> {

    init {
        binding = ItemProcessedImageBinding.bind(view)
    }

    override fun bind(data: Image) {
        binding.apply {
            Glide.with(view.context) // replace with 'this' if it's in activity
                .load(data.input_image_hres_url)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(this?.ivProcessed!!)

            binding?.ivProcessed?.setOnClickListener {
                listener?.onItemClick(
                    it,
                    adapterPosition,
                    data
                )
            }
        }
    }
}