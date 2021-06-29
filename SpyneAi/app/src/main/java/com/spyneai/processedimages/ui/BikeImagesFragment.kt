package com.spyneai.processedimages.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentBikeImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.adapter.ProcessedImagesAdapter
import com.spyneai.processedimages.ui.data.ProcessedViewModel

class BikeImagesFragment : BaseFragment<ProcessedViewModel, FragmentBikeImagesBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        viewModel.getImagesOfSku(
            "18e13dda-ceb8-48f0-8ee0-13fecc26a7f8",
            "sku-50487a81-0c04-460c-8136-5492b8663769"
        )

        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    binding.rvImages.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = ProcessedImagesAdapter(requireContext(),
                            it.value.data as ArrayList<ImagesOfSkuRes.Data>
                        )
                    }
                }

                is Resource.Failure -> {
                   handleApiError(it)
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