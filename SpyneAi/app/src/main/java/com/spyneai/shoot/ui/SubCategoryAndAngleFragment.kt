package com.spyneai.shoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.posthog.android.Properties
import com.spyneai.BaseApplication
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
import com.spyneai.shoot.utils.shoot

class SubCategoryAndAngleFragment : BaseFragment<ShootViewModel,FragmentSelectSubcategoryAndAngleBinding>(),
    OnItemClickListener {


    var subcatAndAngleAdapter : SubcatAndAngleAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isSkuCreated.value == null){
            viewModel.getSubCategories.observe(viewLifecycleOwner,{
                getSubcategories()
            })

            observeSubcategories()
        }else {
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
                        Properties()
                    )

                    binding.apply {
                        shimmer.stopShimmer()
                        shimmer.visibility = View.INVISIBLE
                    }

                    subcatAndAngleAdapter = SubcatAndAngleAdapter(it.value.data,this)

                    binding.rv.apply {
                        layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
                        adapter = subcatAndAngleAdapter
                    }

//                    initAngles()
//                    observeOverlays()

                }
                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_SUBCATRGORIES_FAILED, Properties(),
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
        when(data){
            is NewSubCatResponse.Data -> {

               hideViews()

                viewModel.subCategory.value = data
                if (getString(R.string.app_name) == AppConstants.KARVI){
                    viewModel.exterirorAngles.value = 8
                    //create sku
                    createSku()
                    observerSku()
                }else{
                    selectAngles()
                }
            }
        }
    }

    private fun hideViews() {
        binding.clRoot.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.transparent))

        binding.apply {
            shimmer.stopShimmer()
            shimmer.visibility = View.INVISIBLE
            ivArrow.visibility = View.GONE
            tvDescription.visibility = View.INVISIBLE
            rv.visibility = View.INVISIBLE
        }

    }

    private fun selectAngles() {

        AngleSelectionDialog().show(requireActivity().supportFragmentManager, "AngleSelectionDialog")

    }

    private fun createSku() {
        val createProjectRes = (viewModel.createProjectRes.value as Resource.Success).value

        Utilities.showProgressDialog(requireContext())

        viewModel.createSku(
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            createProjectRes.project_id,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            viewModel.subCategory.value?.prod_sub_cat_id!!,
            viewModel.sku.value?.skuName.toString(),
            viewModel.exterirorAngles.value!!
        )
    }

    private fun observerSku(){
        val createProjectRes = (viewModel.createProjectRes.value as Resource.Success).value
        val projectId = createProjectRes.project_id
        val prod_sub_cat_id =  viewModel.subCategory.value?.prod_sub_cat_id!!

        viewModel.createSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    BaseApplication.getContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name", viewModel.sku.value?.skuName.toString())
                            .putValue("project_id", projectId)
                            .putValue("prod_sub_cat_id", prod_sub_cat_id)
                            .putValue("angles", viewModel.exterirorAngles.value!!)
                    )

                    val sku = viewModel.sku.value
                    sku?.skuId = it.value.sku_id
                    sku?.projectId = projectId
                    sku?.createdOn = System.currentTimeMillis()
                    sku?.totalImages = viewModel.exterirorAngles.value
                    sku?.categoryName = viewModel.categoryDetails.value?.categoryName
                    sku?.categoryId = viewModel.categoryDetails.value?.categoryId
                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
                    sku?.subcategoryId = prod_sub_cat_id
                    sku?.exteriorAngles = viewModel.exterirorAngles.value

                    viewModel.sku.value = sku
                    viewModel.isSubCategoryConfirmed.value = true
                    viewModel.isSkuCreated.value = true
                    viewModel.showLeveler.value = true

                    //add sku to local database
                    viewModel.insertSku(sku!!)

                }


                is Resource.Failure -> {
                    viewModel.isCameraButtonClickable = true
                    BaseApplication.getContext().captureFailureEvent(
                        Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it) { createSku() }
                }
            }
        })
    }
}