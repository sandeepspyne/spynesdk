package com.spyneai.shoot.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.spyneai.*
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.DialogCreateProjectAndSkuBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.Project
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.utils.shoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateProjectAndSkuDialog : BaseDialogFragment<ShootViewModel,DialogCreateProjectAndSkuBinding>() {

    val TAG = CreateProjectAndSkuDialog::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        when(viewModel.categoryDetails.value?.categoryId){
            AppConstants.BIKES_CATEGORY_ID -> {
                binding.iv.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bikes_vin))
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (binding.etVinNumber.text.toString().isEmpty()) {
                if (getString(R.string.app_name) == "Sweep.ie"){
                    binding.etVinNumber.error = "Please enter vehicle number"
                }else{
                    binding.etVinNumber.error = "Please enter any unique number"
                }
            }else if(binding.etVinNumber.text.toString().contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())) {
                binding.etVinNumber.error = "Special characters not allowed"

            }else{
                createProject()
            }
        }

        observeProjectResponse()
    }

    override fun onResume() {
        super.onResume()
        viewModel.createProjectDialogShown = true
    }


    private fun createProject() {

        val project = com.spyneai.shoot.repository.model.project.Project(
            uuid = getUuid(),
            categoryId = viewModel.categoryDetails.value?.categoryId!!,
            categoryName = viewModel.categoryDetails.value?.categoryName!!,
            projectName = removeWhiteSpace(binding.etVinNumber.text.toString())
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

        dismiss()
    }


    private fun observeProjectResponse() {
//        viewModel.createProjectRes.observe(viewLifecycleOwner,{
//            when(it) {
//                is Resource.Success -> {
//                    Utilities.hideProgressDialog()
//
//                    requireContext().captureEvent(
//                        Events.CREATE_PROJECT,
//                        HashMap<String,Any?>()
//                            .apply {
//                                this.put("project_name",  removeWhiteSpace(binding.etVinNumber.text.toString()))
//                            }
//                    )
//
//                    //save project to local db
//                    val project = Project()
//                    project.projectName = removeWhiteSpace(binding.etVinNumber.text.toString())
//                    project.createdOn = System.currentTimeMillis()
//                    project.categoryId = viewModel.categoryDetails.value?.categoryId
//                    project.categoryName = viewModel.categoryDetails.value?.categoryName
//                    project.projectId = it.value.project_id
//                    viewModel.insertProject(project)
//
//                    //update shoot session
//                    Utilities.savePrefrence(requireContext(),AppConstants.SESSION_ID,project.projectId)
//
//                    if (viewModel.sku == null){
//                        val sku = Sku(
//                            uuid = it.value.project_id,
//                            skuName = removeWhiteSpace(binding.etVinNumber.text.toString()).uppercase()
//                        )
//
//                        viewModel.sku = sku
//                    }
//
//                    viewModel.projectId.value = it.value.project_id
//
//                    //notify project created
//                    viewModel.isProjectCreated.value = true
//                    viewModel.getSubCategories.value = true
//                    dismiss()
//                }
//
//                is Resource.Failure -> {
//                    requireContext().captureFailureEvent(Events.CREATE_SKU_FAILED, HashMap<String,Any?>(),
//                        it.errorMessage!!
//                    )
//                    Utilities.hideProgressDialog()
//                    handleApiError(it) { createProject()}
//                }
//            }
//        })
    }


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