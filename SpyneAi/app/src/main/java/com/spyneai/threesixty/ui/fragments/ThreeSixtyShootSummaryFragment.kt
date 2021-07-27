package com.spyneai.threesixty.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.credits.CreditPlansActivity
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.Fragment360ShotSummaryBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtyShootSummaryFragment : BaseFragment<ThreeSixtyViewModel, Fragment360ShotSummaryBinding>() {

    private var availableCredits = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUserCredits()
        observeCredits()

        Glide.with(requireContext()) // replace with 'this' if it's in activity
            .load(viewModel.videoDetails.sample360)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(binding.imageViewGif)

        binding.tvTotalCost.text = viewModel.videoDetails.frames.toString() + " Credits"
        binding.tvSelectedFrames.text = viewModel.videoDetails.frames.toString() + " Frames"

        binding.tvChangeFidelity.setOnClickListener {
            Navigation.findNavController(binding.tvChangeFidelity)
                .navigate(R.id.action_threeSixtyShootSummaryFragment_to_fidelitySelectionFragment2)

            viewModel.title.value = "Change Fidelity"
        }

        binding.tvTopUp.setOnClickListener {
            var intent = Intent(requireContext(), CreditPlansActivity::class.java)
            intent.putExtra("credit_available",availableCredits)
            startActivity(intent)
        }

        binding.btnProceed.setOnClickListener {
            //process image call
           reduceCredits(true)
        }

        observeReduceCredits()
        observepaidStatus()

        viewModel.isFramesUpdated.observe(viewLifecycleOwner,{
            if (it) {
                binding.tvTotalCost.text = viewModel.videoDetails.frames.toString() + " Credits"
                binding.tvSelectedFrames.text = viewModel.videoDetails.frames.toString() + " Frames"
            }
        })
    }


    private fun getUserCredits() {
        viewModel.getUserCredits(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
    }

    private fun reduceCredits(showLoader : Boolean) {
        if (showLoader)
        Utilities.showProgressDialog(requireContext())

        viewModel.reduceCredit(
            Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString(),
            viewModel.videoDetails.frames.toString(),
            "TaD1VC1Ko",
            viewModel.videoDetails.skuId!!
        )
    }

    private fun observeReduceCredits() {
        viewModel.reduceCreditResponse.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    updatePaidStatus(false)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { reduceCredits(true) }
                }
            }
        })
    }

    private fun updatePaidStatus(showLoader : Boolean) {
        if (showLoader)
            Utilities.showProgressDialog(requireContext())


        viewModel.updateDownloadStatus(
            Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString(),
            viewModel.videoDetails.skuId!!,
            "TaD1VC1Ko",
            true
        )
    }

    private fun observepaidStatus() {
        viewModel.downloadHDRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    processSku(false)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { updatePaidStatus(true) }
                }
            }
        })
    }

    private fun observeCredits() {
        viewModel.userCreditsRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    availableCredits = it.value.data.credit_available
                    binding.tvAvailableCredits.text = "$availableCredits Credits"

                    if (availableCredits >= viewModel.videoDetails.frames)
                        binding.btnProceed.isEnabled = true
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getUserCredits() }
                }

                is Resource.Loading -> Utilities.showProgressDialog(requireContext())
            }
        })
    }

    private fun processSku(showLoader : Boolean) {
        if (showLoader)
            Utilities.showProgressDialog(requireContext())

        viewModel.process360(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())

        viewModel.process360Res.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    Navigation.findNavController(binding.btnProceed)
                        .navigate(R.id.action_threeSixtyShootSummaryFragment_to_videoProcessingStartedFragment)

                    viewModel.title.value = "Processing Started"
                    viewModel.processingStarted.value = true
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {processSku(true)}
                }
            }
        })
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = Fragment360ShotSummaryBinding.inflate(inflater,container,false)
}