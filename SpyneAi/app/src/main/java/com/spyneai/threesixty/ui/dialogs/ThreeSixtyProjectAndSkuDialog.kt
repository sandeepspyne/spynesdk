package com.spyneai.threesixty.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.posthog.android.Properties
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
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtyProjectAndSkuDialog : BaseDialogFragment<ThreeSixtyViewModel, DialogCreateProjectAndSkuBinding>() {

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
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            projectName,
            viewModel.categoryDetails.value?.categoryId.toString())

        viewModel.createProjectRes.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.CREATE_360_PROJECT,
                        Properties().putValue("project_name",projectName))


                    viewModel.videoDetails.apply {
                        projectId = it.value.project_id
                        this.skuName = skuName
                    }

                    val subCategory =  viewModel.subCategory.value

                    createSku(it.value.project_id, subCategory?.prod_sub_cat_id.toString())
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Failure -> {
                    dismiss()
                    requireContext().captureFailureEvent(
                        Events.CREATE_360_PROJECT_FAILED, Properties(),
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
            viewModel.videoDetails.skuName.toString()
        )

        viewModel.createSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.CREATE_360_SKU,
                        Properties().putValue("sku_name",viewModel.videoDetails.skuName.toString())
                            .putValue("project_id",projectId)
                            .putValue("prod_sub_cat_id",prod_sub_cat_id))

                    Utilities.hideProgressDialog()

                    viewModel.videoDetails.apply {
                        skuId = it.value.sku_id
                    }


                    //notify project created
                    viewModel.isProjectCreated.value = true

                    dismiss()
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(Events.CREATE_360_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogCreateProjectAndSkuBinding.inflate(inflater, container, false)
}