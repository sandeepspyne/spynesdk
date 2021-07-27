package com.spyneai.processedimages.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.adapter.ShowReplacedImagesAdapter
import com.spyneai.adapter.ShowReplacedImagesFocusedAdapter
import com.spyneai.adapter.ShowReplacedImagesInteriorAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentBikeImagesBinding
import com.spyneai.databinding.FragmentProcessedImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.processedimages.ui.data.ProcessedViewModel

class ProcessedImagesFragment : BaseFragment<ProcessedViewModel, FragmentProcessedImagesBinding>() {

    private lateinit var showReplacedImagesAdapter: ShowReplacedImagesAdapter
    private lateinit var ShowReplacedImagesInteriorAdapter: ShowReplacedImagesInteriorAdapter
    private lateinit var ShowReplacedImagesFocusedAdapter: ShowReplacedImagesFocusedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSkuImages()

        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {getSkuImages()}
                }

                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    val imagesList = it.value.data

                    val exteriorImageList = imagesList?.filter {
                        it.image_category == "Exterior"
                    }

                    val interiorImageList = imagesList?.filter {
                        it.image_category == "Interior"
                    }

                    val focusedImageList = imagesList?.filter {
                        it.image_category == "Focus Shoot"
                    }

                }
            }
        })
    }

    private fun getSkuImages() {
        Utilities.showProgressDialog(requireContext())

        viewModel.getImagesOfSku(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            viewModel.skuId!!
        )
    }

    override fun getViewModel() = ProcessedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProcessedImagesBinding.inflate(inflater, container, false)


}