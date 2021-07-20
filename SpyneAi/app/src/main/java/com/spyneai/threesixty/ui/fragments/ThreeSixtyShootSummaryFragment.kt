package com.spyneai.threesixty.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.Fragment360IntroBinding
import com.spyneai.databinding.Fragment360ShotSummaryBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtyShootSummaryFragment : BaseFragment<ThreeSixtyViewModel, Fragment360ShotSummaryBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext()) // replace with 'this' if it's in activity
            .load(viewModel.videoDetails.sample360)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(binding.imageViewGif)


        binding.btnProceed.setOnClickListener {


            //process image call
            processSku()
        }
    }

    private fun processSku() {
        viewModel.process360(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())

        viewModel.process360Res.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    Navigation.findNavController(binding.btnProceed)
                        .navigate(R.id.action_threeSixtyShootSummaryFragment_to_videoProcessingStartedFragment)

                    viewModel.title.value = "Processing Started"
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {processSku()}
                }

                is Resource.Loading -> Utilities.showProgressDialog(requireContext())
            }
        })
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = Fragment360ShotSummaryBinding.inflate(inflater,container,false)
}