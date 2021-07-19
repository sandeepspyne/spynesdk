package com.spyneai.shoot.ui.dialogs

import android.content.Context
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
import com.spyneai.shoot.data.model.Sku

class CreateProjectAndSkuDialog : BaseDialogFragment<ShootViewModel, DialogCreateProjectAndSkuBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.ivEditProjectName.setOnClickListener {
            val imm: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
            binding.etProjectName.requestFocus()

            binding.etProjectName.setSelection(binding.etProjectName.text.toString().length)
        }

        binding.btnSubmit.setOnClickListener {
            when {
                binding.etProjectName.text.toString().isEmpty() -> binding.etProjectName.error = "Please enter project name"
                binding.etVinNumber.text.toString().isEmpty() -> {
                    binding.etVinNumber.error = "Please enter any unique number"
                }
                else -> {
                    createProject(
                        binding.etProjectName.text.toString(),
                        binding.etVinNumber.text.toString()
                    )
                }
            }
        }
    }

    private fun createProject(projectName: String, skuName: String) {
        viewModel.createProject(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            projectName,
            viewModel.categoryDetails.value?.categoryId.toString()
        )

        viewModel.createProjectRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.CREATE_PROJECT,
                        Properties().putValue("project_name", projectName)
                    )

                    Utilities.hideProgressDialog()
                    //notify project created
                    viewModel.isProjectCreated.value = true
                    val sku = Sku()
                    sku.projectId = it.value.project_id
                    sku.skuName = skuName
                    viewModel.sku.value = sku

                    dismiss()
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Failure -> {
                    dismiss()
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

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogCreateProjectAndSkuBinding.inflate(inflater, container, false)
}