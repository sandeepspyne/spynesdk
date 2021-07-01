package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide

import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.DialogFocusedHintBinding
import com.spyneai.shoot.data.ShootViewModel

class MiscShotsDialog : BaseDialogFragment<ShootViewModel, DialogFocusedHintBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)


       if (viewModel.categoryDetails.value?.categoryName != "Automobiles") {
           setSampleImages()
       }

        binding.tvSkipFocused.setOnClickListener {
            viewModel.selectBackground.value = true
            dismiss()
        }

        binding.tvShootNowFocused.setOnClickListener {
            viewModel.startMiscShots.value = true
            dismiss()
        }
    }

    private fun setSampleImages() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    val miscList = it.value.miscellaneous
                    if (miscList.isNotEmpty() && miscList.size > 3){
                        loadImage(miscList[0].display_thumbnail,binding.ivFirst)
                        loadImage(miscList[1].display_thumbnail,binding.ivSecond)
                        loadImage(miscList[2].display_thumbnail,binding.ivThird)
                        loadImage(miscList[3].display_thumbnail,binding.ivFourth)
                    }
                }
                else -> {}
            }
        })
    }

    private fun loadImage(url: String, imageView: ImageView) {
        Glide.with(requireContext())
            .load(url)
            .into(imageView)
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogFocusedHintBinding.inflate(inflater, container, false)
}