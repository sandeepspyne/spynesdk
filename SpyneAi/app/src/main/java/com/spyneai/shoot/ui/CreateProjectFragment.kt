package com.spyneai.shoot.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.posthog.android.Properties
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentCreateProjectBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.dialogs.CreateProjectAndSkuDialog
import com.spyneai.shoot.ui.dialogs.ShootHintDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateSkuEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ProjectTagDialog
import com.spyneai.shoot.utils.log

class CreateProjectFragment : BaseFragment<ShootViewModel, FragmentCreateProjectBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            if (viewModel.projectId.value == null){
                if(Utilities.getPreference(requireContext(), AppConstants.STATUS_PROJECT_NAME).toString() =="true")
                    getProjectName()
                else
                    initProjectDialog(true)
            }
            else {
                if (viewModel.fromDrafts){
                    if (viewModel.isSkuCreated.value == null
                        && viewModel.isSubCategoryConfirmed.value == null)
                        initSkuDialog()
                }else {
                    if (viewModel.isSkuCreated.value == null)
                        initSkuDialog()

                }
            }
        }else{
            viewModel.showHint.observe(viewLifecycleOwner,{
                if (!viewModel.gifDialogShown)
                    initShootHint()
            })

            viewModel.showVin.observe(viewLifecycleOwner,{
                if (!viewModel.createProjectDialogShown)
                    initProjectDialog(false)
            })
        }
    }

    private fun initShootHint() {
        requireContext().captureEvent(Events.SHOW_HINT, HashMap<String,Any?>())
        ShootHintDialog().show(requireActivity().supportFragmentManager, "ShootHintDialog")
    }

    private fun initProjectDialog(isPortrait : Boolean) {
        if (isPortrait){
            ProjectTagDialog().show(requireFragmentManager(), "CreateProjectEcomDialog")
        }else{
            CreateProjectAndSkuDialog().show(
                requireActivity().supportFragmentManager,
                "CreateProjectAndSkuDialog"
            )
        }

    }

    private fun initSkuDialog() {
        CreateSkuEcomDialog().show(requireFragmentManager(), "CreateSkuEcomDialog")
    }

    private fun getProjectName(){

        viewModel.getProjectName(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())

        viewModel.getProjectNameResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {

                    Utilities.hideProgressDialog()

                    viewModel.dafault_project.value = it.value.data.dafault_project
                    viewModel.dafault_sku.value = it.value.data.dafault_sku
                    initProjectDialog(true)
                    log("project and SKU dialog shown")
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    log("get project name failed")
                    requireContext().captureFailureEvent(
                        Events.CREATE_PROJECT_FAILED, HashMap<String,Any?>(),
                        it.errorMessage!!
                    )

                    Utilities.hideProgressDialog()
                    handleApiError(it) { getProjectName()}
                }
            }
        })

    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCreateProjectBinding.inflate(inflater, container, false)
}