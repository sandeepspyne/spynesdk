package com.spyneai.shoot.ui.dialogs

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.spyneai.shoot.data.model.Project
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.utils.shoot

class CreateProjectAndSkuDialog : BaseDialogFragment<ShootViewModel, DialogCreateProjectAndSkuBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)
        binding.btnSubmit.setOnClickListener {
            if (binding.etVinNumber.text.toString().isEmpty()) {
                binding.etVinNumber.error = "Please enter any unique number"
            }else if(binding.etVinNumber.text.toString().contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())) {
                binding.etVinNumber.error = "Special characters not allowed"

            }else{
                createProject()
            }
        }

        observeProjectResponse()
    }

    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")

    private fun createProject() {

        Utilities.showProgressDialog(requireContext())

        viewModel.createProject(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            removeWhiteSpace(binding.etVinNumber.text.toString()),
            viewModel.categoryDetails.value?.categoryId.toString()
        )

    }

    private fun observeProjectResponse() {
        viewModel.createProjectRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.CREATE_PROJECT,
                        Properties().putValue("project_name",  removeWhiteSpace(binding.etVinNumber.text.toString()))
                    )

                    //save project to local db
                    val project = Project()
                    project.projectName = removeWhiteSpace(binding.etVinNumber.text.toString())
                    project.createdOn = System.currentTimeMillis()
                    project.categoryId = viewModel.categoryDetails.value?.categoryId
                    project.categoryName = viewModel.categoryDetails.value?.categoryName
                    project.projectId = it.value.project_id
                    viewModel.insertProject(project)

                    Utilities.hideProgressDialog()
                    //notify project created
                    viewModel.isProjectCreated.value = true
                    val sku = Sku()
                    sku.projectId = it.value.project_id
                    sku.skuName =  removeWhiteSpace(binding.etVinNumber.text.toString())
                    viewModel.sku.value = sku

                    dismiss()
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.CREATE_PROJECT_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it) { createProject()}
                }
            }
        })
    }

//    override fun onStop() {
//        super.onStop()
//        shoot("onStop called(createProjectAndSkuDialog-> dismissAllowingStateLoss)")
//        dismissAllowingStateLoss()
//    }

    override fun onDestroy() {
        super.onDestroy()
        shoot("onDestroy called(shootHintDialog)")
        dismissAllowingStateLoss()
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissAllowingStateLoss()
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogCreateProjectAndSkuBinding.inflate(inflater, container, false)
}