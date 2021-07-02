package com.spyneai.processedimages.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.activity.DownloadingActivity
import com.spyneai.activity.OrderSummary2Activity

import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentBikeImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.adapter.ProcessedImagesAdapter
import com.spyneai.processedimages.ui.data.ProcessedViewModel

class BikeImagesFragment : BaseFragment<ProcessedViewModel, FragmentBikeImagesBinding>(),
    OnItemClickListener {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.getImagesOfSku(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            viewModel.skuId!!
        )

        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    binding.rvImages.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = ProcessedImagesAdapter(requireContext(),
                            it.value.data as ArrayList<ImagesOfSkuRes.Data>,
                            this@BikeImagesFragment
                        )
                    }

                    var imageList = ArrayList<String>()

                    it.value.data.forEach {
                        imageList.add(it.input_image_hres_url)
                    }

                    binding.llDownloadHdImages.setOnClickListener {
                        val downloadIntent = Intent(requireContext(), BikeOrderSummaryActivity::class.java)
                        Utilities.savePrefrence(requireContext(), AppConstants.DOWNLOAD_TYPE, "hd")
                        downloadIntent.putExtra(AppConstants.LIST_HD_QUALITY, imageList)
                        downloadIntent.putExtra(AppConstants.LIST_WATERMARK, imageList)
                        downloadIntent.putExtra("is_paid",requireActivity().intent.getBooleanExtra("is_paid",false))
                        downloadIntent.putExtra(AppConstants.SKU_ID, viewModel.skuId)
                        downloadIntent.putExtra(AppConstants.SKU_NAME,"")
                        downloadIntent.putExtra(
                            AppConstants.IS_DOWNLOADED_BEFORE,
                            requireActivity().intent.getBooleanExtra("is_paid",false)
                        )
                        startActivity(downloadIntent)
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                   handleApiError(it)
                }
            }
        })



    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        val itemData = data as ImagesOfSkuRes.Data
        viewModel.selectedImageUrl = itemData.input_image_hres_url

        ProcessedImageDialog().show(requireActivity().supportFragmentManager,"ProcessedImageDialog")
    }

    override fun getViewModel() = ProcessedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentBikeImagesBinding.inflate(inflater, container, false)


}