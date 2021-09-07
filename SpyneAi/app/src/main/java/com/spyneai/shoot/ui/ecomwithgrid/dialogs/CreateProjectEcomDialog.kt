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
import com.spyneai.shoot.data.model.Project
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_credit_plans.*

class CreateProjectEcomDialog :
    BaseDialogFragment<ShootViewModel, CreateProjectEcomDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.ivClose.setOnClickListener {
            requireActivity().finish()
        }

        binding.btnProceed.setOnClickListener {

            when {
                binding.etProjectName.text.toString().isEmpty() -> {
                    binding.etProjectName.error =
                        "Please enter project name"
                }
                binding.etProjectName.text.toString().contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) -> {
                    binding.etProjectName.error = "Special characters not allowed"
                }
                binding.etSkuName.text.toString().isEmpty() -> {
                    binding.etSkuName.error = "Please enter product name"
                }
                binding.etSkuName.text.toString().contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) -> {
                    binding.etSkuName.error = "Special characters not allowed"
                }
                else -> {
                    log("create project started")
                    log("project name: "+binding.etProjectName.text.toString())
                    log("sku name: "+binding.etSkuName.text.toString())
                    createProject()
                }
            }
        }

        if (!viewModel.fromDrafts) {
            observeCreateProject()
            observeCreateSku()
        }
    }

    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")


    private fun createProject() {
        Utilities.showProgressDialog(requireContext())

        viewModel.createProject(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            removeWhiteSpace(binding.etProjectName.text.toString()),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )
    }

    private fun observeCreateProject() {
        viewModel.createProjectRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.CREATE_PROJECT,
                        Properties().putValue("project_name", removeWhiteSpace( binding.etProjectName.text.toString()))
                    )

                    //save project to local db
                    val project = Project()
                    project.projectName = removeWhiteSpace(binding.etProjectName.text.toString())
                    project.createdOn = System.currentTimeMillis()
                    project.categoryId = viewModel.categoryDetails.value?.categoryId
                    project.categoryName = viewModel.categoryDetails.value?.categoryName
                    project.projectId = it.value.project_id
                    viewModel.insertProject(project)

                    //notify project created
                    viewModel.isProjectCreated.value = true
                    val sku = Sku()
                    log("project id created")
                    log("project id: "+it.value.project_id)
                    sku.projectId = it.value.project_id
                    viewModel.projectId.value = it.value.project_id
                    Utilities.savePrefrence(requireContext(), AppConstants.PROJECT_ID, it.value.project_id)
                    sku.skuName = removeWhiteSpace(binding.etSkuName.text.toString())
                    viewModel.sku.value = sku

                    log("create sku started")
                    createSku(it.value.project_id, removeWhiteSpace(binding.etSkuName.text.toString()),false)
                }

                is Resource.Failure -> {
                    log("create project id failed")
                    requireContext().captureFailureEvent(
                        Events.CREATE_PROJECT_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it) {createProject()}
                }
            }
        })
    }

    private fun createSku(projectId: String, skuName: String,showDialog: Boolean) {
        if (showDialog)
            Utilities.showProgressDialog(requireContext())

        viewModel.createSku(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            projectId,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            "",
            skuName,
            0
        )
    }

    private fun observeCreateSku() {
        viewModel.createSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    requireContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name", viewModel.sku.value?.skuName.toString())
                            .putValue("project_id", viewModel.sku.value?.projectId)
                            .putValue("prod_sub_cat_id", "")
                    )

                    //notify project created
                    val sku = viewModel.sku.value
                    sku?.skuId = it.value.sku_id
                    sku?.projectId = viewModel.sku.value?.projectId
                    sku?.createdOn = System.currentTimeMillis()
                    sku?.totalImages = viewModel.exterirorAngles.value
                    sku?.categoryName = viewModel.categoryDetails.value?.categoryName
                    sku?.categoryId = viewModel.categoryDetails.value?.categoryId
                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
                    sku?.subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id
                    sku?.exteriorAngles = viewModel.exterirorAngles.value

                    log("sku id created")
                    log("sku id: "+it.value.sku_id)
                    sku?.skuName = removeWhiteSpace(binding.etSkuName.text.toString())
                    viewModel.sku.value = sku
                    viewModel.isSkuCreated.value = true
                    //viewModel.isSubCategoryConfirmed.value = true
                    viewModel.showLeveler.value = true

                    //add sku to local database
                    viewModel.insertSku(sku!!)

                    dismiss()
                }


                is Resource.Failure -> {
                    log("create sku id failed")
                    Utilities.hideProgressDialog()
                    requireContext().captureFailureEvent(
                        Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )

                    handleApiError(it) {createSku(viewModel.sku.value?.projectId!!,
                        removeWhiteSpace(binding.etSkuName.text.toString()),
                        true)}
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