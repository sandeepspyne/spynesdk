package com.spyneai.shoot.ui.ecomwithgrid

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentProjectDetailBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shoot.adapters.ProjectDetailAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.repository.model.project.ProjectWithSkuAndImages
import com.spyneai.shoot.ui.SelectBackgroundFragment
import com.spyneai.shoot.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

        //update sku
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.setProjectAndSkuData(
                viewModel.project?.uuid!!,
                viewModel.sku?.uuid!!
            )
        }

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
            AppConstants.SPYNE_AI, AppConstants.SPYNE_AI_AUTOMOBILE -> {
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
                        AppConstants.SPYNE_AI,AppConstants.SPYNE_AI_AUTOMOBILE -> {
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
                }
            }
        }

        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun processWithoutBackgroundId() {
        Utilities.showProgressDialog(requireContext())

        GlobalScope.launch(Dispatchers.IO) {
            //viewModel.updateProjectStatus()

            viewModel.updateBackground(5000)

            GlobalScope.launch(Dispatchers.Main) {

                Utilities.hideProgressDialog()

                //start sync service
                requireContext().startUploadingService(
                    SelectBackgroundFragment::class.java.simpleName,
                    ServerSyncTypes.PROCESS
                )

                requireContext().captureEvent(
                    Events.PROCESS,
                    HashMap<String, Any?>()
                        .apply {
                            this.put("sku_id", viewModel.sku?.uuid!!)
                            this.put("background_id", "none")
                        }
                )

                requireContext().gotoHome()
            }
        }
    }


    private fun processWithShadowOption() {
       GlobalScope.launch(Dispatchers.IO) {
           viewModel.updateBackground(5000)

           GlobalScope.launch(Dispatchers.Main) {

               Utilities.hideProgressDialog()

               //start sync service
               requireContext().startUploadingService(
                   SelectBackgroundFragment::class.java.simpleName,
                   ServerSyncTypes.PROCESS
               )

               requireContext().captureEvent(
                   Events.PROCESS,
                   HashMap<String, Any?>()
                       .apply {
                           this.put("sku_id", viewModel.sku?.uuid!!)
                           this.put("background_id", 5000)
                       }
               )

               requireContext().gotoHome()
           }
       }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.tvTotalSkuCaptured.text = viewModel.project?.skuCount.toString()
        binding.tvTotalImageCaptured.text = viewModel.project?.imagesCount.toString()

        val dataList = ArrayList<ProjectWithSkuAndImages>()

       GlobalScope.launch(Dispatchers.IO) {
           val list = viewModel.getProjectSkus()

           list.forEach {
               dataList.add(
                   ProjectWithSkuAndImages(
                       it,
                       viewModel.getImagesbySkuId(it.uuid)
                   )
               )
           }

           GlobalScope.launch(Dispatchers.Main) {
               projectDetailAdapter = ProjectDetailAdapter(
                   requireContext(),
                   dataList
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
       }




//        viewModel.projectDetailResponse.observe(viewLifecycleOwner, {
//            when (it) {
//                is Resource.Success -> {
//                    Utilities.hideProgressDialog()
//
//                    it.value.data.sku
//
//                    projectDetailAdapter = ProjectDetailAdapter(
//                        requireContext(),
//                        it.value.data.sku
//                    )
//
//                    binding.rvParentProjects.apply {
//                        this?.layoutManager =
//                            LinearLayoutManager(
//                                requireContext(),
//                                LinearLayoutManager.VERTICAL,
//                                false
//                            )
//                        this?.adapter = projectDetailAdapter
//                    }
//                }
//
//                is Resource.Failure -> {
//                    Utilities.hideProgressDialog()
//                    handleApiError(it)
//                }
//            }
//        })
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