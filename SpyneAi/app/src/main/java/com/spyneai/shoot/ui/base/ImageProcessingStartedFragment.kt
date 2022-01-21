package com.spyneai.shoot.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentImageProcessingStartedBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ProcessViewModel


class ImageProcessingStartedFragment : BaseFragment<ProcessViewModel, FragmentImageProcessingStartedBinding>()  {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arguments?.let {
            if (it.getString(AppConstants.CATEGORY_ID) == AppConstants.CARS_CATEGORY_ID){
                if (getString(R.string.app_name) == AppConstants.SPYNE_AI){
                    Glide.with(this).asGif().load(R.raw.image_processing_started)
                        .into(binding.ivProcessing)
                }else {
                    Glide.with(this).load(R.drawable.app_logo)
                        .into(binding.ivProcessing)
                }
            }else{
                Glide.with(this).load(R.drawable.app_logo)
                    .into(binding.ivProcessing)
            }
        }

        binding.llHome.setOnClickListener {
            requireContext().gotoHome()
        }
    }

    override fun onResume() {
        super.onResume()

//        if (viewModel.categoryId == AppConstants.BIKES_CATEGORY_ID){
//            if (viewModel.interiorMiscShootsCount > 0){
//                observeTotalFrameUpdate()
//                updateTotalFrames()
//            }
//
//
//            viewModel.updateProjectState(
//                Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
//                viewModel.sku?.projectId!!
//            )
//        }
    }

    private fun observeTotalFrameUpdate() {
        viewModel.updateTotalFramesRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    val properties = HashMap<String,Any?>()
                    properties.apply {
                        this["sku_id"] = viewModel.sku?.skuId!!
                        this["total_frames"] = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)
                    }

                    requireContext().captureEvent(Events.TOTAL_FRAMES_UPDATED,properties)

                    Utilities.hideProgressDialog()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()

                    val properties = HashMap<String,Any?>()
                    properties.apply {
                        this["sku_id"] = viewModel.sku?.skuId!!
                        this["total_frames"] = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)
                    }

                    requireContext().captureFailureEvent(
                        Events.TOTAL_FRAMES_UPDATE_FAILED,properties,
                        it.errorMessage!!)

                    handleApiError(it) { updateTotalFrames()}
                }
            }
        })
    }


    private fun updateTotalFrames() {
        Utilities.showProgressDialog(requireContext())

        val totalFrames = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)

        viewModel.updateCarTotalFrames(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            viewModel.sku?.skuId!!,
            totalFrames.toString()
        )
    }

    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentImageProcessingStartedBinding.inflate(inflater, container, false)
}