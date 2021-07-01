package com.spyneai.shoot.ui.ecom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.posthog.android.Properties
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.CreateProjectEcomDialogBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.Sku

class CreateProjectEcomDialog :
    BaseDialogFragment<ShootViewModel, CreateProjectEcomDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.btnProceed.setOnClickListener {
            when {
                binding.etProjectName.text.toString().isEmpty() -> binding.etProjectName.error =
                    "Please enter project name"
                binding.etSkuName.text.toString().isEmpty() -> {
                    binding.etSkuName.error = "Please enter product name"
                }
                else -> {
                    createProject(
                        binding.etProjectName.text.toString(),
                        binding.etSkuName.text.toString()
                    )
                }
            }
        }
    }

    private fun createProject(projectName: String, skuName: String) {
        viewModel.createProject(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            projectName,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )

        viewModel.createProjectRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Sucess -> {
                    requireContext().captureEvent(
                        Events.CREATE_PROJECT,
                        Properties().putValue("project_name", projectName)
                    )

                    //notify project created
                    viewModel.isProjectCreated.value = true
                    val sku = Sku()
                    sku.projectId = it.value.project_id
                    sku.skuName = skuName
                    viewModel.sku.value = sku

                    createSku(it.value.project_id, skuName)


                }

                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.CREATE_PROJECT_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })

    }

    private fun createSku(projectId: String, skuName: String) {
        viewModel.createSku(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            projectId,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            "",
            skuName
        )

        viewModel.createSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Sucess -> {
                    requireContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name", viewModel.sku.value?.skuName.toString())
                            .putValue("project_id", projectId)
                            .putValue("prod_sub_cat_id", "")
                    )

                    //notify project created
                    viewModel.isSkuCreated.value = true

                    Utilities.hideProgressDialog()
                    val sku = viewModel.sku.value
                    sku?.skuId = it.value.sku_id
                    sku?.skuName = skuName

                    viewModel.sku.value = sku
                    viewModel.isSubCategoryConfirmed.value = true

                    //add sku to local database
                    viewModel.insertSku(sku!!)
                    dismiss()
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())

                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = CreateProjectEcomDialogBinding.inflate(inflater, container, false)

}