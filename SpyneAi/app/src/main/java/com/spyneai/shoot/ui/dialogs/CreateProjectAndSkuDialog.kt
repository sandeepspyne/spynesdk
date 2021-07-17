package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.DialogCreateProjectAndSkuBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.Sku

class CreateProjectAndSkuDialog : BaseDialogFragment<ShootViewModel,DialogCreateProjectAndSkuBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.btnSubmit.setOnClickListener {
            when {
                binding.etProjectName.text.toString().isEmpty() -> binding.etProjectName.error = "Please enter project name"
                binding.etVinNumber.text.toString().isEmpty() -> {
                    binding.etVinNumber.error = "Please enter any unique number"
                }
                else -> {
                    createProject(binding.etProjectName.text.toString(),binding.etVinNumber.text.toString())
                }
            }
        }
    }

    private fun createProject(projectName : String,skuName : String) {
        viewModel.createProject(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            projectName,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString())

        viewModel.createProjectRes.observe(viewLifecycleOwner,{
            when(it){
                    is Resource.Success -> {
                        requireContext().captureEvent(
                            Events.CREATE_PROJECT,
                            Properties().putValue("project_name",projectName))

                        val sku = Sku()
                        sku.projectId = it.value.project_id
                        sku.skuName = skuName
                        viewModel.sku.value = sku
                        val subCategory =  viewModel.subCategory.value
                        if (getString(R.string.app_name) != "Karvi.com"){
                            Utilities.hideProgressDialog()
                            //notify project created
                            viewModel.isProjectCreated.value = true
                            dismiss()
                        }else{
                            createSku(it.value.project_id, subCategory?.prod_sub_cat_id.toString())
                        }

                    }

                    is Resource.Loading -> {
                        Utilities.showProgressDialog(requireContext())
                    }

                    is Resource.Failure -> {
                        requireContext().captureFailureEvent(Events.CREATE_PROJECT_FAILED, Properties(),
                            it.errorMessage!!
                        )
                        Utilities.hideProgressDialog()
                        handleApiError(it)
                    }
            }
        })
    }

    private fun createSku(projectId: String, prod_sub_cat_id : String) {
        viewModel.createSku(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),projectId,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            prod_sub_cat_id!!,
            viewModel.sku.value?.skuName.toString(),
            8
        )

        viewModel.createSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name",viewModel.sku.value?.skuName.toString())
                            .putValue("project_id",projectId)
                            .putValue("prod_sub_cat_id",prod_sub_cat_id))

                    Utilities.hideProgressDialog()
                    val sku = viewModel.sku.value
                    sku?.skuId = it.value.sku_id
                    sku?.totalImages = 8

                    viewModel.sku.value = sku
                    //notify project created
                    viewModel.isProjectCreated.value = true

                    //add sku to local database
                    viewModel.insertSku(sku!!)
                    dismiss()
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogCreateProjectAndSkuBinding.inflate(inflater, container, false)
}