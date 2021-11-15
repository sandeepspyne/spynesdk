package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.posthog.android.Properties
import com.spyneai.*
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.DialogAngleSelectionBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.shoot

class AngleSelectionDialog : BaseDialogFragment<ShootViewModel,DialogAngleSelectionBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false
        refreshTexts()

        showOptions()
    }

    private fun showOptions() {
        val angles: String = getString(R.string.angles)
        val valuesShoots = when(getString(R.string.app_name)){
            AppConstants.CARS24_INDIA,AppConstants.CARS24 -> arrayOf("5 "+angles)
            AppConstants.SELL_ANY_CAR -> arrayOf("4 "+angles, "36 "+angles)
            else -> arrayOf("8 "+angles, "12 "+angles,"16 "+angles,"24 "+angles,"36 "+angles)
        }

        val lastSelectedAngles = viewModel.getSelectedAngles(getString(R.string.app_name))
        var newSelectedAngles = viewModel.getSelectedAngles(getString(R.string.app_name))


        when(getString(R.string.app_name)){
            AppConstants.SELL_ANY_CAR->{
                when(viewModel.getSelectedAngles(getString(R.string.app_name))){
                    4 -> binding.npShoots.minValue = 0
                    36 -> binding.npShoots.minValue = 1
                }
            } else -> {
            when(viewModel.getSelectedAngles(getString(R.string.app_name))){
                8,5 -> binding.npShoots.minValue = 0
                12 -> binding.npShoots.minValue = 1
                16 -> binding.npShoots.minValue = 2
                24 -> binding.npShoots.minValue = 3
                36 -> binding.npShoots.minValue = 4
            }
        }
        }

        binding.npShoots.minValue = 0
        binding.npShoots.maxValue = valuesShoots.size - 1
        binding.npShoots.displayedValues = valuesShoots

        binding.npShoots.setOnValueChangedListener { _, _, newVal ->
            when(valuesShoots[newVal]) {
                "4 "+angles -> newSelectedAngles = 4
                "5 "+angles -> newSelectedAngles = 5
                "8 "+angles -> newSelectedAngles = 8
                "12 "+angles -> newSelectedAngles = 12
                "16 "+angles -> newSelectedAngles = 16
                "24 "+angles -> newSelectedAngles = 24
                "36 "+angles -> newSelectedAngles = 36
            }
        }
        binding.tvProceed.setOnClickListener {
            viewModel.exterirorAngles.value = newSelectedAngles

            //create sku
            val createProjectRes = (viewModel.createProjectRes.value as Resource.Success).value

            if (viewModel.fromVideo){
                updateSku()
                observerUpdateSku()
            }else{
                createSku(
                    createProjectRes.project_id,
                    viewModel.subCategory.value?.prod_sub_cat_id!!
                )
            }
        }
    }


    private fun refreshTexts() {
        requireContext().setLocale()
        binding.tvChooseShot.text = getString(R.string.choose_shoots)
        binding.tvMoreAngle.text = getString(R.string.more_angles)
        binding.tvProceed.text = getString(R.string.proceed)
    }

    private fun observerUpdateSku() {
        val createProjectRes = (viewModel.createProjectRes.value as Resource.Success).value

        viewModel.updateVideoSkuRes.observe(viewLifecycleOwner,{
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    BaseApplication.getContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name", viewModel.sku.value?.skuName.toString())
                            .putValue("project_id", createProjectRes.project_id)
                            .putValue("prod_sub_cat_id", viewModel.subCategory.value?.prod_sub_cat_id!!)
                            .putValue("angles", viewModel.exterirorAngles.value!!)
                    )

                    val sku = viewModel.sku.value
                    sku?.skuId =  viewModel.sku.value?.skuId!!
                    sku?.projectId = createProjectRes.project_id
                    sku?.createdOn = System.currentTimeMillis()
                    sku?.totalImages = viewModel.exterirorAngles.value
                    sku?.categoryName = viewModel.categoryDetails.value?.categoryName
                    sku?.categoryId = viewModel.categoryDetails.value?.categoryId
                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
                    sku?.subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id!!
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
                    handleApiError(it) { updateSku() }
                }
            }
        })
    }

    private fun updateSku() {
        Utilities.showProgressDialog(requireContext())

        viewModel.updateVideoSku(
            viewModel.sku.value?.skuId!!,
            viewModel.subCategory.value?.prod_sub_cat_id!!,
            viewModel.exterirorAngles.value!!
        )
    }

    private fun createSku(projectId: String, prod_sub_cat_id: String) {
        Utilities.showProgressDialog(requireContext())

        viewModel.createSku(
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            projectId,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            prod_sub_cat_id!!,
            viewModel.sku.value?.skuName.toString(),
            viewModel.exterirorAngles.value!!
        )

        val s = ""

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

                    val s = ""
                    dismiss()
                }


                is Resource.Failure -> {
                    viewModel.isCameraButtonClickable = true
                    BaseApplication.getContext().captureFailureEvent(
                        Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it) { createSku(projectId, prod_sub_cat_id) }
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        shoot("onStop called(angleSlectionDialog-> dismissAllowingStateLoss)")
        dismissAllowingStateLoss()
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogAngleSelectionBinding.inflate(inflater, container, false)
}