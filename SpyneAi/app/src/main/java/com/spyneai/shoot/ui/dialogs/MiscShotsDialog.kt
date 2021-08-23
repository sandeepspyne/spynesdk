package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.spyneai.R

import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.databinding.DialogFocusedHintBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.shoot

class MiscShotsDialog : BaseDialogFragment<ShootViewModel, DialogFocusedHintBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startInteriorShoot.value = false

        dialog?.setCancelable(false)

       if (viewModel.categoryDetails.value?.categoryName != "Automobiles") {
           setSampleImages()
       }

        binding.tvSkipFocused.setOnClickListener {
            selectBackground()
            dismiss()
        }

        binding.tvShootNowFocused.setOnClickListener {
            viewModel.startMiscShots.value = true
            dismiss()
        }
    }

    private fun selectBackground() {
        if(getString(R.string.app_name) == AppConstants.OLA_CABS)
            viewModel.show360InteriorDialog.value = true
        else
            viewModel.selectBackground.value = true
    }

    private fun setSampleImages() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    //filter misc shots
                    val filteredList: List<NewSubCatResponse.Miscellaneous> = it.value.miscellaneous.filter {
                        it.prod_sub_cat_id ==  viewModel.subCategory.value?.prod_sub_cat_id
                    }

                    it.value.miscellaneous = filteredList

                    if (filteredList.isNotEmpty() && filteredList.size > 3){
                        loadImage(filteredList[0].display_thumbnail,binding.ivFirst)
                        loadImage(filteredList[1].display_thumbnail,binding.ivSecond)
                        loadImage(filteredList[2].display_thumbnail,binding.ivThird)
                        loadImage(filteredList[3].display_thumbnail,binding.ivFourth)
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


//    override fun onStop() {
//        super.onStop()
//        shoot("onStop called(miscShotsDialog-> dismissAllowingStateLoss)")
//        dismissAllowingStateLoss()
//    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogFocusedHintBinding.inflate(inflater, container, false)
}