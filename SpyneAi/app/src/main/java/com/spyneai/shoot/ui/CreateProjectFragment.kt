package com.spyneai.shoot.ui

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentCreateProjectBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.sdk.Spyne
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.ui.dialogs.CreateProjectAndSkuDialog
import com.spyneai.shoot.ui.dialogs.ShootHintDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateSkuEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ProjectTagDialog
import com.spyneai.shoot.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateProjectFragment : BaseFragment<ShootViewModel, FragmentCreateProjectBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Utilities.getBool(requireContext(),AppConstants.FROM_SDK,false)){
            createProject()
        }else {
            if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                if (viewModel.project == null){
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
                    if (viewModel.fromVideo){
                        if (!viewModel.createProjectDialogShown && viewModel.isProjectCreated.value == false)
                            initProjectDialog(false)
                    }else {
                        if (!viewModel.createProjectDialogShown)
                            initProjectDialog(false)
                    }

                })
            }
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

    private fun createProject() {

        val project = com.spyneai.shoot.repository.model.project.Project(
            uuid = getUuid(),
            categoryId = viewModel.categoryDetails.value?.categoryId!!,
            categoryName = viewModel.categoryDetails.value?.categoryName!!,
            projectName = Spyne.foreignSkuId
        )


        viewModel.project = project

        //update shoot session
        Utilities.savePrefrence(requireContext(),AppConstants.SESSION_ID,project.uuid)

        if (viewModel.sku == null){
            val sku = Sku(
                uuid = getUuid(),
                projectUuid = project.uuid,
                categoryId = project.categoryId,
                categoryName = project.categoryName,
                skuName = project.projectName
            )
            viewModel.sku = sku

            GlobalScope.launch(Dispatchers.IO) {
                if (Utilities.getBool(requireContext(),AppConstants.FROM_SDK,false)){
                    project.isCreated = true
                    sku.isCreated = true
                }
                Log.d(TAG, "createProject: "+project.uuid)
                val id = viewModel.insertProject()
                Log.d(TAG, "createProject: $id")
                viewModel.insertSku()
            }
        }

        viewModel.projectId.value = project.uuid
        //notify project created
        viewModel.isProjectCreated.value = true
        viewModel.getSubCategories.value = true
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCreateProjectBinding.inflate(inflater, container, false)
}