package com.spyneai.shoot.ui.ecomwithgrid

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentProjectDetailBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.ProjectDetailAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ShadowOptionDialog
import com.spyneai.shoot.utils.log

class ProjectDetailFragment : BaseFragment<ShootViewModel, FragmentProjectDetailBinding>() {

    lateinit var projectDetailAdapter: ProjectDetailAdapter
    var refreshData = true
    lateinit var handler: Handler
    private var runnable: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

//        binding.swiperefreshProject.setOnRefreshListener {
//            repeatRefreshData()
//            binding.swiperefreshProject.isRefreshing = false
//        }

        when (getString(R.string.app_name)) {
            AppConstants.SWIGGY -> {
                binding.btHome.text = "Select Background"
            }
            AppConstants.EBAY -> {
                binding.btHome.text = "Proceed"
            }
        }

        binding.btHome.setOnClickListener {
            when (viewModel.categoryDetails.value?.categoryName) {
                "Food & Beverages" -> {
                    viewModel.showFoodBackground.value = true
                }
                else -> {
                    when(getString(R.string.app_name)){
                        AppConstants.SWIGGYINSTAMART, AppConstants.FLIPKART_GROCERY -> {
                            processWithBackgroundId()
                        }
                        AppConstants.EBAY -> {
                            requireContext().captureEvent(Events.SHOW_SHADOW_DIALOG, Properties())
                            ShadowOptionDialog().show(requireActivity().supportFragmentManager, "ShadowOptionDialog")
                        }else -> {
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
                    handleApiError(it)  {
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