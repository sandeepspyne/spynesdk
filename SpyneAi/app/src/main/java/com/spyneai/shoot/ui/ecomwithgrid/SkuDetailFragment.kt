package com.spyneai.shoot.ui.ecomwithgrid

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.posthog.android.Properties
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSkuDetailBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.SkuImageAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.base.ShootPortraitActivity
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.EndProjectDialog
import com.spyneai.shoot.utils.log


class SkuDetailFragment : BaseFragment<ShootViewModel, FragmentSkuDetailBinding>() {

    lateinit var skuImageAdapter: SkuImageAdapter
    var totalSkuImages = 0
    var endProject = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (viewModel.categoryDetails.value?.categoryName) {
            "Footwear" -> {
                binding.ivAddAngle.visibility = View.INVISIBLE
                binding.tvAddAngle.visibility = View.INVISIBLE
            }
        }

        viewModel.getProjectDetail(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.projectId.value!!
        )

        viewModel.projectDetailResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    viewModel.totalSkuCaptured.value = it.value.data.total_sku.toString()
                    viewModel.totalImageCaptured.value = it.value.data.total_images.toString()

                    binding.tvTotalSkuCaptured.text = it.value.data.total_sku.toString()
                }


                is Resource.Loading -> {

                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })

        viewModel.updateTotalFramesRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    log("update total images for sku(" + viewModel.sku.value?.skuId.toString() + "): " + totalSkuImages.toString())

                }
                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())

                }
                is Resource.Failure -> {
                    log("update total images for sku(" + viewModel.sku.value?.skuId.toString() + ") failed")
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })

        viewModel.shootList.observe(viewLifecycleOwner, {
            try {

                totalSkuImages = it.size

                binding.tvTotalImageCaptured.text = it.size.toString()


                skuImageAdapter = SkuImageAdapter(
                    requireContext(),
                    it
                )

                binding.rvSkuImages.apply {
                    this?.layoutManager =
                        GridLayoutManager(requireContext(), 3)
                    this?.adapter = skuImageAdapter
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        binding.btNextSku.setOnClickListener {
            log("update total frames")
            log("skuId: " + viewModel.sku.value?.skuId.toString())
            log("totalFrames: " + viewModel.shootList.value?.size.toString())
            log(
                "authKey: " + Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY)
                    .toString()
            )
            endProject = false
            updateTotalFrames()
        }

        binding.ivAddAngle.setOnClickListener {
            viewModel.addMoreAngle.value = true
        }

        binding.tvEndProject.setOnClickListener {
            endProject = true
            updateTotalFrames()
        }

        observeTotalFrameUpdate()

        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun updateTotalFrames() {
        viewModel.updateTotalFrames(
            viewModel.sku.value?.skuId.toString(),
            totalSkuImages.toString(),
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString()
        )
    }

    private fun observeTotalFrameUpdate() {
        viewModel.updateTotalFramesRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    val properties = HashMap<String,Any?>()
                    properties.apply {
                        this["sku_id"] = viewModel.sku.value?.skuId!!
                        this["total_frames"] = totalSkuImages.toString()
                    }

                    requireContext().captureEvent(Events.TOTAL_FRAMES_UPDATED, properties)

                    Utilities.hideProgressDialog()
                    processRequest()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()

                    val properties = HashMap<String,Any?>()
                    properties.apply {
                        this["sku_id"] = viewModel.sku.value?.skuId!!
                        this["total_frames"] = totalSkuImages.toString()
                    }

                    requireContext().captureFailureEvent(
                        Events.TOTAL_FRAMES_UPDATE_FAILED, properties,
                        it.errorMessage!!
                    )

                    handleApiError(it) { updateTotalFrames() }
                }
            }
        })
    }

    private fun processRequest() {
        if (endProject) {
            log("end project dialog called")
            EndProjectDialog().show(requireFragmentManager(), "EndProjectDialog")
        } else {
            observeUpdateTotalFrames()


        }


        binding.ivAddAngle.setOnClickListener {
            if (viewModel.categoryDetails.value?.categoryName.equals("E-Commerce") || viewModel.categoryDetails.value?.categoryName.equals(
                    "Food & Beverages"
                )
            )
                viewModel.addMoreAngle.value = true
        }
        binding.tvAddAngle.setOnClickListener {
            if (viewModel.categoryDetails.value?.categoryName.equals("E-Commerce") || viewModel.categoryDetails.value?.categoryName.equals(
                    "Food & Beverages"
                )
            )
                viewModel.addMoreAngle.value = true
        }
    }

    private fun observeUpdateTotalFrames() {
        viewModel.updateTotalFramesRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    viewModel.shootList.value?.clear()
                    val intent = Intent(activity, ShootPortraitActivity::class.java)
                    intent.putExtra("project_id", viewModel.projectId.value);
                    intent.putExtra("skuNumber", viewModel.skuNumber.value?.plus(1)!!);

                    intent.putExtra(
                        AppConstants.CATEGORY_NAME,
                        viewModel.categoryDetails.value?.categoryName
                    )
                    intent.putExtra(
                        AppConstants.CATEGORY_ID,
                        viewModel.categoryDetails.value?.categoryId
                    )

                    if (viewModel.fromDrafts) {
                        intent.putExtra(
                            AppConstants.SKU_COUNT,
                            requireActivity().intent.getIntExtra(AppConstants.SKU_COUNT, 0).plus(1)
                        )
                        intent.putExtra(
                            "skuNumber",
                            requireActivity().intent.getIntExtra(AppConstants.SKU_COUNT, 0).plus(1)
                        )
                    } else
                        intent.putExtra("skuNumber", viewModel.skuNumber.value?.plus(1)!!)
                    startActivity(intent)
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_BACKGROUND_FAILED, HashMap<String,Any?>(),
                        it.errorMessage!!
                    )
                    handleApiError(it) {}
                }

                is Resource.Loading -> {

                }
            }
        })
    }


    override fun onResume() {
        super.onResume()

        if (viewModel.categoryDetails.value?.categoryName.equals("E-Commerce")) {
            binding.ivAddAngle.visibility = View.VISIBLE
            binding.tvAddAngle.visibility = View.VISIBLE
        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSkuDetailBinding.inflate(inflater, container, false)

}