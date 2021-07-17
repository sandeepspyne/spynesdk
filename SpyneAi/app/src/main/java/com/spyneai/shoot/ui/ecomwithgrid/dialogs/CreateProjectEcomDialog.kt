package com.spyneai.shoot.ui.ecomwithgrid.dialogs

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
import com.spyneai.shoot.utils.log

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
                    log("create project started")
                    log("project name: "+binding.etProjectName.text.toString())
                    log("sku name: "+binding.etSkuName.text.toString())
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
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.CREATE_PROJECT,
                        Properties().putValue("project_name", projectName)
                    )

                    //notify project created
                    viewModel.isProjectCreated.value = true
                    val sku = Sku()
                    log("project id created")
                    log("project id: "+it.value.project_id)
                    sku.projectId = it.value.project_id
                    Utilities.savePrefrence(requireContext(), AppConstants.PROJECT_ID, it.value.project_id)
                    sku.skuName = skuName
                    viewModel.sku.value = sku

                    log("create sku started")
                    createSku(it.value.project_id, skuName)
                }

                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Failure -> {
                    dismiss()
                    log("create project id failed")
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
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    requireContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name", viewModel.sku.value?.skuName.toString())
                            .putValue("project_id", projectId)
                            .putValue("prod_sub_cat_id", "")
                    )

                    //notify project created
                    val sku = viewModel.sku.value
                    sku?.skuId = it.value.sku_id
                    log("sku id created")
                    log("sku id: "+it.value.sku_id)
                    sku?.skuName = skuName
                    viewModel.sku.value = sku
                    viewModel.isSkuCreated.value = true

                    //add sku to local database
//                    viewModel.insertSku(sku!!)
                    dismiss()
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())

                }

                is Resource.Failure -> {
                    dismiss()
                    log("create sku id failed")
                    Utilities.hideProgressDialog()
                    requireContext().captureFailureEvent(
                        Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )

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