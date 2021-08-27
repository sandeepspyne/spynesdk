package com.spyneai.shoot.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentImageProcessingStartedBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ProcessViewModel


class ImageProcessingStartedFragment : BaseFragment<ProcessViewModel, FragmentImageProcessingStartedBinding>()  {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (getString(R.string.app_name) == AppConstants.SPYNE_AI){
            Glide.with(this).asGif().load(R.raw.image_processing_started)
                .into(binding.ivProcessing)
        }else {
            Glide.with(this).load(R.drawable.app_logo)
                .into(binding.ivProcessing)
        }


        binding.llHome.setOnClickListener {
            requireContext().gotoHome()
        }

        if (viewModel.categoryName == "Bikes"){
            if (viewModel.interiorMiscShootsCount > 0)
                updateTotalFrames()

            viewModel.updateProjectState(
                Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
                viewModel.sku.value?.projectId!!
            )
        }



    }
    private fun updateTotalFrames() {
        val totalFrames = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)

        viewModel.updateCarTotalFrames(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            viewModel.sku.value?.skuId!!,
            totalFrames.toString()
        )
    }

    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentImageProcessingStartedBinding.inflate(inflater, container, false)
}