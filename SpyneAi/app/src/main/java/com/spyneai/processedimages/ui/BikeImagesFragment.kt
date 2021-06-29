package com.spyneai.processedimages.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.FragmentBikeImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.processedimages.ui.data.ProcessedViewModel

class BikeImagesFragment : BaseFragment<ProcessedViewModel, FragmentBikeImagesBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getImagesOfSku(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            "sku_id"
        )

        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Loading -> {

                }

                is Resource.Success -> {

                }

                is Resource.Failure -> {

                }
            }
        })
    }


    override fun getViewModel() = ProcessedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentBikeImagesBinding.inflate(inflater, container, false)
}