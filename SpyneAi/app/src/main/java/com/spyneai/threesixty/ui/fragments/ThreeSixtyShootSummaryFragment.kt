package com.spyneai.threesixty.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
                is Resource.Success -> Toast.makeText(requireContext(),"Processed", Toast.LENGTH_LONG).show()
                is Resource.Failure -> handleApiError(it) {processSku()}
                else -> {}
            }
        })
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = Fragment360ShotSummaryBinding.inflate(inflater,container,false)
}