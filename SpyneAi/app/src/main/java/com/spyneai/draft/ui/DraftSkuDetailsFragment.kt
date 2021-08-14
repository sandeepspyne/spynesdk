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
import com.spyneai.shoot.ui.base.ShootActivity

class DraftSkuDetailsFragment : BaseFragment<DraftViewModel, FragmentDraftSkuDetailsBinding>() {

    private var exterior = ArrayList<ImagesOfSkuRes.Data>()
    private var interiorList = ArrayList<ImagesOfSkuRes.Data>()
    private var miscList = ArrayList<ImagesOfSkuRes.Data>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSkuDetails()

        observeSkuDeatils()

        val intent = requireActivity().intent

        binding.btnContinueShoot.setOnClickListener{
            Intent(
                context,
                ShootActivity::class.java
            ).apply {
                putExtra(AppConstants.FROM_DRAFTS, true)
                putExtra(AppConstants.CATEGORY_ID, "cat_d8R14zUNE")
                putExtra(AppConstants.PROJECT_ID, intent.getStringExtra(AppConstants.PROJECT_ID))
                putExtra(AppConstants.SUB_CAT_ID, intent.getStringExtra(AppConstants.SUB_CAT_ID))
                putExtra(AppConstants.SUB_CAT_NAME,intent.getStringExtra(AppConstants.SUB_CAT_NAME))
                putExtra(AppConstants.CATEGORY_NAME, intent.getStringExtra(AppConstants.CATEGORY_NAME))
                putExtra(AppConstants.SKU_NAME, intent.getStringExtra(AppConstants.SKU_NAME))
                putExtra(AppConstants.SKU_CREATED, true)
                putExtra(AppConstants.SKU_ID, intent.getStringExtra(AppConstants.SKU_ID))
                putExtra(AppConstants.EXTERIOR_ANGLES, intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0))
                putExtra(AppConstants.RESUME_EXTERIOR, resumeExterior())
                putExtra(AppConstants.EXTERIOR_ANGLES, intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0))
                putExtra(AppConstants.RESUME_EXTERIOR, resumeExterior())
                putExtra(AppConstants.EXTERIOR_SIZE, exterior.size)
                putExtra(AppConstants.RESUME_INTERIOR, resumeInterior())
                putExtra(AppConstants.INTERIOR_SIZE, interiorList.size)
                putExtra(AppConstants.RESUME_MISC, resumeMisc())
                putExtra(AppConstants.MISC_SIZE, miscList.size)
                putExtra("is_paid",false)
                putExtra(AppConstants.IMAGE_TYPE,intent.getStringExtra(AppConstants.IMAGE_TYPE))
                putExtra(AppConstants.IS_360,intent.getStringExtra(AppConstants.IS_360))
                startActivity(this)
            }
        }
    }

    private fun resumeMisc() = miscList.size > 0

    private fun resumeInterior() = !resumeExterior() && !resumeMisc()

    private fun resumeExterior() = exterior.size != requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0)


    private fun getSkuDetails() {
        binding.shimmerCompletedSKU.visibility = View.VISIBLE
        binding.shimmerCompletedSKU.startShimmer()

        viewModel.getImagesOfSku(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.SKU_ID)!!
        )
    }

    private  fun observeSkuDeatils() {
        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    binding.shimmerCompletedSKU.stopShimmer()
                    binding.shimmerCompletedSKU.visibility = View.GONE
                    binding.nsv.visibility = View.VISIBLE

                    var imageList = ArrayList<String>()

                    if (!it.value.data.isNullOrEmpty()) {

                        val list = it.value.data

                        exterior = list?.filter {
                            it.image_category == "Exterior"
                        } as ArrayList

                        if (exterior.size > 0) {
                            binding.tvExterior.visibility = View.VISIBLE
                            binding.rvExteriorImage.apply {
                                layoutManager = GridLayoutManager(requireContext(),2,LinearLayoutManager.VERTICAL,false)
                                adapter = DraftImagesAdapter(requireContext(),exterior)
                            }
                        }

                        interiorList = list?.filter {
                            it.image_category == "Interior"
                        } as ArrayList

                        if (interiorList.size > 0) {
                            binding.tvInterior.visibility = View.VISIBLE
                            binding.rvInteriors.apply {
                                layoutManager = GridLayoutManager(requireContext(),2,LinearLayoutManager.VERTICAL,false)
                                adapter = DraftImagesAdapter(requireContext(),interiorList)
                            }
                        }

                        miscList = list?.filter {
                            it.image_category == "Focus Shoot"
                        } as ArrayList

                        if (miscList.size > 0) {
                            binding.tvFocused.visibility = View.VISIBLE
                            binding.rvFocused.apply {
                                layoutManager = GridLayoutManager(requireContext(),2,LinearLayoutManager.VERTICAL,false)
                                adapter = DraftImagesAdapter(requireContext(),miscList)
                            }
                        }

                        binding.flContinueShoot.visibility = View.VISIBLE
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
                    if (it.errorCode == 404){
                        binding.flContinueShoot.visibility = View.VISIBLE
                    }else{
                        handleApiError(it) { getSkuDetails() }
                    }

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