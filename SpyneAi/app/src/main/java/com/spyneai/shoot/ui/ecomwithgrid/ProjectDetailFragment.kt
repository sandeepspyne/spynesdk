package com.spyneai.shoot.ui.ecomwithgrid

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentProjectDetailBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.ProjectDetailAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.log

class ProjectDetailFragment : BaseFragment<ShootViewModel, FragmentProjectDetailBinding>() {

    lateinit var projectDetailAdapter: ProjectDetailAdapter
    var refreshData = true
    lateinit var handler: Handler
    private var runnable: Runnable? = null
    var shadow = "false"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()
        binding.tvShadowOption.text = "Shadow is OFF"

//        binding.swiperefreshProject.setOnRefreshListener {
//            repeatRefreshData()
//            binding.swiperefreshProject.isRefreshing = false
//        }

        binding.switchShadowOption.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                shadow = "true"
                binding.tvShadowOption.text = "Shadow is ON"
            } else {
                shadow = "false"
                binding.tvShadowOption.text = "Shadow is OFF"
            }
        }

        when (getString(R.string.app_name)) {

            AppConstants.FLIPKART, AppConstants.UDAAN, AppConstants.AMAZON, AppConstants.SWIGGY, AppConstants.EBAY  -> {
                when (viewModel.categoryDetails.value?.categoryName) {
                    "Photo Box" -> {
                        binding.groupShadow.visibility = View.VISIBLE
                        binding.btHome.text = "Submit and Process this Project"
                    }
                    "E-Commerce" -> {
                        binding.groupShadow.visibility = View.GONE
                        binding.btHome.text = "Submit Project"
                    }
                    "Footwear" -> {
                        binding.groupShadow.visibility = View.GONE
                        binding.btHome.text = "Submit Project"
                    }
                    "Food & Beverages" -> {
                        binding.groupShadow.visibility = View.GONE
                        binding.btHome.text = "Select Background"
                    }
                }
            }
            AppConstants.SPYNE_AI -> {
                when (viewModel.categoryDetails.value?.categoryName) {
                    "Photo Box" -> {
                        binding.groupShadow.visibility = View.VISIBLE
                        binding.btHome.text = "Submit and Process this Project"
                    }
                    "E-Commerce" -> {
                        binding.btHome.text = "Submit Project"
                        if (Utilities.getPreference(requireContext(),AppConstants.ENTERPRISE_ID)
                            == AppConstants.FLIPKART_ENTERPRISE_ID) {
                            binding.groupShadow.visibility = View.GONE

                        }else{
                            binding.groupShadow.visibility = View.VISIBLE

                        }
                    }
                    "Footwear" -> {
                        binding.groupShadow.visibility = View.GONE
                        binding.btHome.text = "Submit Project"
                    }
                    "Food & Beverages" -> {
                        binding.groupShadow.visibility = View.GONE
                        binding.btHome.text = "Select Background"
                    }
                }
            }
        }

        binding.btHome.setOnClickListener {
            when (viewModel.categoryDetails.value?.categoryName) {
                "Food & Beverages" -> {
                    viewModel.showFoodBackground.value = true
                }
                else -> {
                    when (getString(R.string.app_name)) {
                        AppConstants.SPYNE_AI -> {
                            when (viewModel.categoryDetails.value?.categoryId) {
                                AppConstants.PHOTO_BOX_CATEGORY_ID,
                                AppConstants.ECOM_CATEGORY_ID,
                                AppConstants.CAPS_CATEGORY_ID,
                                AppConstants.FASHION_CATEGORY_ID,
                                AppConstants.FOOD_AND_BEV_CATEGORY_ID,
                                AppConstants.HEALTH_AND_BEAUTY_CATEGORY_ID,
                                AppConstants.ACCESSORIES_CATEGORY_ID,
                                AppConstants.WOMENS_FASHION_CATEGORY_ID,
                                AppConstants.MENS_FASHION_CATEGORY_ID-> {
                                    processWithShadowOption()
                                }
                                AppConstants.FOOTWEAR_CATEGORY_ID -> {
                                    processWithoutBackgroundId()
                                }
                            }
                        }
                        AppConstants.AMAZON, AppConstants.SWIGGYINSTAMART,
                        AppConstants.FLIPKART_GROCERY, AppConstants.UDAAN,
                        AppConstants.FLIPKART, AppConstants.EBAY -> {
                            when (viewModel.categoryDetails.value?.categoryId) {
                                AppConstants.PHOTO_BOX_CATEGORY_ID, -> {
                                    processWithShadowOption()
                                }
                                AppConstants.ECOM_CATEGORY_ID,
                                AppConstants.FOOTWEAR_CATEGORY_ID-> {
                                    processWithoutBackgroundId()
                                }

                            }
                        }
                        else -> {
                            processWithoutBackgroundId()
                        }
                    }
                    log(
                        "auth key- " + Utilities.getPreference(
                            requireContext(),
                            AppConstants.AUTH_KEY
                        ).toString()
                    )
                    log("project id- " + viewModel.projectId.value)
                    log("skuProcessState called")
                }
            }
        }

        viewModel.skuProcessStateResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    requireContext().gotoHome()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {
                        processWithoutBackgroundId()
                    }
                }
            }
        })

        viewModel.skuProcessStateWithBgResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    requireContext().gotoHome()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {
                        processWithBackgroundId()
                    }
                }
            }
        })

        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun processWithoutBackgroundId() {
        Utilities.showProgressDialog(requireContext())
        viewModel.skuProcessState(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.projectId.value.toString()
        )
    }

    private fun processWithBackgroundId() {
        Utilities.showProgressDialog(requireContext())
        viewModel.skuProcessStateWithBackgroundid(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.projectId.value.toString(),
            5000
        )
    }

    private fun processWithShadowOption() {
        Utilities.showProgressDialog(requireContext())
        viewModel.skuProcessStateWithShadowOption(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.projectId.value.toString(),
            5000,
            shadow
        )


        viewModel.skuProcessStateWithShadowResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    requireContext().gotoHome()
                }

                is Resource.Failure -> {
                    log("create project id failed")
                    requireContext().captureFailureEvent(
                        Events.SKU_PROCESS_STATE_WITH_SHADOW_FAILED, HashMap<String,Any?>(),
                        it.errorMessage!!
                    )

                    Utilities.hideProgressDialog()
                    handleApiError(it) { processWithShadowOption() }
                }
            }
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        repeatRefreshData()
        viewModel.projectDetailResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    it.value.data.sku

                    binding.tvTotalSkuCaptured.text = it.value.data.total_sku.toString()
                    binding.tvTotalImageCaptured.text = it.value.data.total_images.toString()

                    projectDetailAdapter = ProjectDetailAdapter(
                        requireContext(),
                        it.value.data.sku
                    )

                    binding.rvParentProjects.apply {
                        this?.layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        this?.adapter = projectDetailAdapter
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })
    }

    fun repeatRefreshData() {
        try {
            viewModel.getProjectDetail(
                Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
                viewModel.projectId.value.toString()
            )
            runnable = Runnable {
                if (refreshData)
                    repeatRefreshData()
            }
            if (runnable != null)
                handler.postDelayed(runnable!!, 15000)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        if (runnable != null)
            handler.removeCallbacks(runnable!!)
        super.onPause()
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProjectDetailBinding.inflate(inflater, container, false)

}