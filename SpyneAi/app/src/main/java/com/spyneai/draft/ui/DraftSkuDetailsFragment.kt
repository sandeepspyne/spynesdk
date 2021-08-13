package com.spyneai.draft.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.posthog.android.Properties
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentDraftProjectsBinding
import com.spyneai.databinding.FragmentDraftSkuDetailsBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.DraftImagesAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.posthog.Events
import com.spyneai.processedimages.ui.BikeOrderSummaryActivity
import com.spyneai.processedimages.ui.adapter.ProcessedImagesAdapter

class DraftSkuDetailsFragment : BaseFragment<DraftViewModel, FragmentDraftSkuDetailsBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getImagesOfSku(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
           requireActivity().intent.getStringExtra("sku_id")!!
        )

        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    binding.shimmerCompletedSKU.stopShimmer()
                    binding.shimmerCompletedSKU.visibility = View.GONE
                    binding.nsv.visibility = View.VISIBLE


                    var imageList = ArrayList<String>()

                    if (!it.value.data.isNullOrEmpty()) {

                        val list = it.value.data

                        val exterior = list?.filter {
                            it.image_category == "Exterior"
                        } as ArrayList

                        if (exterior.size > 0) {
                            binding.tvExterior.visibility = View.VISIBLE
                            binding.rvExteriorImage.apply {
                                layoutManager = GridLayoutManager(requireContext(),2,LinearLayoutManager.VERTICAL,false)
                                adapter = DraftImagesAdapter(requireContext(),exterior)
                            }
                        }

                        val interiorList = list?.filter {
                            it.image_category == "Interior"
                        } as ArrayList

                        if (interiorList.size > 0) {
                            binding.tvInterior.visibility = View.VISIBLE
                            binding.rvInteriors.apply {
                                layoutManager = GridLayoutManager(requireContext(),2,LinearLayoutManager.VERTICAL,false)
                                adapter = DraftImagesAdapter(requireContext(),interiorList)
                            }
                        }

                        val miscList = list?.filter {
                            it.image_category == "Focus Shoot"
                        } as ArrayList

                        if (miscList.size > 0) {
                            binding.tvFocused.visibility = View.VISIBLE
                            binding.rvFocused.apply {
                                layoutManager = GridLayoutManager(requireContext(),2,LinearLayoutManager.VERTICAL,false)
                                adapter = DraftImagesAdapter(requireContext(),miscList)
                            }
                        }
                    }

                    it.value.data.forEach {
                        imageList.add(it.input_image_hres_url)
                    }
                }

                is Resource.Failure -> {
                    binding.shimmerCompletedSKU.stopShimmer()
                    binding.shimmerCompletedSKU.visibility = View.GONE

                    requireContext().captureFailureEvent(
                        Events.GET_COMPLETED_ORDERS_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    handleApiError(it)
                }
            }
        })
    }

    override fun getViewModel() = DraftViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDraftSkuDetailsBinding.inflate(inflater, container, false)

}