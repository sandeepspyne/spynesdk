package com.spyneai.shoot.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSelectSubcategoryAndAngleBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.SubcatAndAngleAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SubCategoryAndAngleFragment :
    BaseFragment<ShootViewModel, FragmentSelectSubcategoryAndAngleBinding>(),
    OnItemClickListener {


    var subcatAndAngleAdapter: SubcatAndAngleAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        viewModel.getSubCategories.observe(viewLifecycleOwner, {
//            getSubcategories()
//        })
//
//        observeSubcategories()


        when (viewModel.categoryDetails.value?.categoryId) {
            AppConstants.FOOTWEAR_CATEGORY_ID -> binding.tvDescription.text =
                getString(R.string.footwear_subcategory)
            AppConstants.BIKES_CATEGORY_ID -> binding.tvDescription.text =
                getString(R.string.bikes_subcategory)
            else -> {
                binding.tvDescription.text = "Select your ${viewModel.categoryDetails.value?.categoryName}'s Sub-categories"
            }
        }

        if (viewModel.isSkuCreated.value == null || (viewModel.categoryDetails.value?.categoryId != AppConstants.CARS_CATEGORY_ID
                    &&
                    viewModel.categoryDetails.value?.categoryId != AppConstants.BIKES_CATEGORY_ID)
        ) {
            viewModel.getSubCategories.observe(viewLifecycleOwner, {
                getSubcategories()
            })

            observeSubcategories()
        } else {
            hideViews()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.isSubcategoriesSelectionShown = true
    }

    private fun getSubcategories() {
        binding.shimmer.startShimmer()

        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )
    }

    fun observeSubcategories() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.GET_SUBCATEGORIES,
                        HashMap<String, Any?>()
                    )

                    binding.apply {
                        shimmer.stopShimmer()
                        shimmer.visibility = View.INVISIBLE
                    }

                    subcatAndAngleAdapter = SubcatAndAngleAdapter(it.value.data, this)

                    val lyManager =
                        if (requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        else
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )

                    binding.rv.apply {
                        layoutManager = lyManager
                        adapter = subcatAndAngleAdapter
                    }
                }
                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_SUBCATRGORIES_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )
                    binding.shimmer.stopShimmer()

                    handleApiError(it) { getSubcategories() }
                }
            }
        })
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectSubcategoryAndAngleBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is NewSubCatResponse.Subcategory -> {

                hideViews()

                viewModel.subCategory.value = data
                viewModel.subCatName.value = data.sub_cat_name

                when (getString(R.string.app_name)) {
                    AppConstants.SPYNE_AI, AppConstants.SPYNE_AI_AUTOMOBILE -> {
                        when (viewModel.categoryDetails.value?.categoryName) {
                            "Automobiles", "Bikes" -> {
                                selectAngles()
                            }
                            else -> {
                                viewModel.exterirorAngles.value = 0

                                updateSku()
                                observerUpdateSku()
                            }
                        }
                    }
                    AppConstants.KARVI,AppConstants.AUTO_FOTO, AppConstants.CARS24, AppConstants.CARS24_INDIA -> {
                        viewModel.exterirorAngles.value = 8
                        //create sku
                        if (viewModel.fromVideo) {
                            updateSku()
                            observerUpdateSku()
                        } else {
                            createSku()
                            observerSku()
                        }
                    }

                    AppConstants.AMAZON -> {
                        viewModel.exterirorAngles.value = 0

                        updateSku()
                        observerUpdateSku()
                    }
                    else -> selectAngles()
                }

            }
        }
    }

    private fun observerUpdateSku() {
//        val createProjectRes = (viewModel.createProjectRes.value as Resource.Success).value
//
//        viewModel.updateVideoSkuRes.observe(viewLifecycleOwner, {
//            when (it) {
//                is Resource.Success -> {
//                    Utilities.hideProgressDialog()
//
//                    BaseApplication.getContext().captureEvent(
//                        Events.CREATE_SKU,
//                        HashMap<String, Any?>()
//                            .apply {
//                                this.put("sku_name", viewModel.sku?.skuName.toString())
//                                this.put("project_id", createProjectRes.project_id)
//                                this.put(
//                                    "prod_sub_cat_id",
//                                    viewModel.subCategory.value?.prod_sub_cat_id!!
//                                )
//                                this.put("angles", viewModel.exterirorAngles.value!!)
//                            }
//                    )
//
//                    val sku = viewModel.sku
//                    sku?.skuId = viewModel.sku?.skuId!!
//                    sku?.projectId = createProjectRes.project_id
//                    //sku?.createdOn = System.currentTimeMillis()
//                    //sku?.totalImages = viewModel.exterirorAngles.value
//                    sku?.categoryName = viewModel.categoryDetails.value?.categoryName
//                    sku?.categoryId = viewModel.categoryDetails.value?.categoryId
//                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
//                    sku?.subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id!!
//                    //sku?.exteriorAngles = viewModel.exterirorAngles.value
//
//                    viewModel.sku = sku
//                    viewModel.isSubCategoryConfirmed.value = true
//                    viewModel.isSkuCreated.value = true
//
//                    when (viewModel.categoryDetails.value?.categoryId) {
//                        AppConstants.BIKES_CATEGORY_ID,
//                        AppConstants.CARS_CATEGORY_ID,
//                        AppConstants.FOOTWEAR_CATEGORY_ID,
//                        AppConstants.FOOD_AND_BEV_CATEGORY_ID-> {
//                            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
//                            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
//                            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
//                        }
//                        else -> {
//                            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
//                            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
//                            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
//                        }
//                    }
//
//                    //update sku locally
//                    //viewModel.updateVideoSkuLocally(sku!!)
//
//                }
//
//
//                is Resource.Failure -> {
//                    viewModel.isCameraButtonClickable = true
//                    BaseApplication.getContext().captureFailureEvent(
//                        Events.CREATE_SKU_FAILED, HashMap<String, Any?>(),
//                        it.errorMessage!!
//                    )
//                    Utilities.hideProgressDialog()
//                    handleApiError(it) { updateSku() }
//                }
//            }
//        })
    }

    private fun updateSku() {
        setSubcategoryData()

        viewModel.isSubCategoryConfirmed.value = true
        viewModel.isSkuCreated.value = true
        viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
        viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
        viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

        //add sku to local database
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.updateSubcategory()
        }
    }

    private fun hideViews() {
        binding.clRoot.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.transparent
            )
        )

        binding.apply {
            shimmer.stopShimmer()
            shimmer.visibility = View.INVISIBLE
            ivArrow?.visibility = View.GONE
            tvDescription.visibility = View.INVISIBLE
            rv.visibility = View.INVISIBLE
        }
    }

    private fun selectAngles() {
        setSubcategoryData()

        AngleSelectionDialog().show(
            requireActivity().supportFragmentManager,
            "AngleSelectionDialog"
        )
    }

    private fun setSubcategoryData(){
        viewModel.sku?.apply {
            subcategoryName = viewModel.subCategory.value?.sub_cat_name
            subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id
            initialFrames = viewModel.exterirorAngles.value
            totalFrames = viewModel.exterirorAngles.value
        }
    }

    private fun createSku() {
        setSubcategoryData()

        viewModel.isSubCategoryConfirmed.value = true
        viewModel.isSkuCreated.value = true
        viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
        viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
        viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

        //add sku to local database
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.updateSubcategory()
        }
    }

    private fun observerSku() {
//        val createProjectRes = (viewModel.createProjectRes.value as Resource.Success).value
//        val projectId = createProjectRes.project_id
//        val prod_sub_cat_id = viewModel.subCategory.value?.prod_sub_cat_id!!
//
//        viewModel.createSkuRes.observe(viewLifecycleOwner, {
//            when (it) {
//                is Resource.Success -> {
//                    Utilities.hideProgressDialog()
//
//                    BaseApplication.getContext().captureEvent(
//                        Events.CREATE_SKU,
//                        HashMap<String, Any?>()
//                            .apply {
//                                this.put("sku_name", viewModel.sku?.skuName.toString())
//                                this.put("project_id", projectId)
//                                this.put("prod_sub_cat_id", prod_sub_cat_id)
//                                this.put("angles", viewModel.exterirorAngles.value!!)
//                            }
//                    )
//
//                    val sku = viewModel.sku
//                    sku?.skuId = it.value.sku_id
//                    sku?.projectId = projectId
//                   // sku?.createdOn = System.currentTimeMillis()
//                    //sku?.totalImages = viewModel.exterirorAngles.value
//                    sku?.categoryName = viewModel.categoryDetails.value?.categoryName
//                    sku?.categoryId = viewModel.categoryDetails.value?.categoryId
//                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
//                    sku?.subcategoryId = prod_sub_cat_id
//                   // sku?.exteriorAngles = viewModel.exterirorAngles.value
//
//                    viewModel.sku = sku
//                    viewModel.isSubCategoryConfirmed.value = true
//                    viewModel.isSkuCreated.value = true
////                    viewModel.showLeveler.value = true
//                    viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
//                    viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
//                    viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
//
//                    //add sku to local database
//                   GlobalScope.launch {
//                       viewModel.insertSku(sku!!)
//                   }
//
//                }
//
//
//                is Resource.Failure -> {
//                    viewModel.isCameraButtonClickable = true
//                    BaseApplication.getContext().captureFailureEvent(
//                        Events.CREATE_SKU_FAILED, HashMap<String, Any?>(),
//                        it.errorMessage!!
//                    )
//                    Utilities.hideProgressDialog()
//                    handleApiError(it) { createSku() }
//                }
//            }
//        })
    }
}