package com.spyneai.threesixty.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.*
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.DialogCreateProjectAndSkuBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shoot.data.model.Project
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.threesixty.data.model.VideoDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ThreeSixtyProjectAndSkuDialog : BaseDialogFragment<ThreeSixtyViewModel, DialogCreateProjectAndSkuBinding>() {

    private var projectId = ""
    private var prod_sub_cat_id = ""
    private var sku = ""
    private var TAG = "ThreeSixtyProjectAndSkuDialog"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.btnSubmit.setOnClickListener {
            if (binding.etVinNumber.text.toString().isEmpty()) {
                binding.etVinNumber.error = "Please enter any unique number"
            }else if(binding.etVinNumber.text.toString().contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())) {
                binding.etVinNumber.error = "Special characters not allowed"

                if (getString(R.string.app_name) == "Sweep.ie"){
                    binding.etVinNumber.error = "Please enter vehicle number"
                }else{
                    binding.etVinNumber.error = "Please enter any unique number"
                }
            }else if(binding.etVinNumber.text.toString().contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())) {
                binding.etVinNumber.error = "Special characters not allowed"

            }else{
                createProject(
                    removeWhiteSpace(binding.etVinNumber.text.toString()),
                    removeWhiteSpace(binding.etVinNumber.text.toString())
                )
            }
        }

        //observeCreateProject()
        //observeSku()
    }

    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")


    private fun createProject(projectName : String,name : String) {

        viewModel.videoDetails?.let {
            val project = com.spyneai.shoot.repository.model.project.Project(
                uuid = it.projectUuid,
                categoryId = it.categoryId,
                categoryName = it.categoryName,
                projectName = projectName
            )

            viewModel.project = project
        }

        //update shoot session
        Utilities.savePrefrence(requireContext(),AppConstants.SESSION_ID,viewModel.videoDetails?.uuid)

        if (viewModel.sku == null){

            viewModel.videoDetails?.let {
                it.type = "360_exterior"
                it.skuName=name
                val sku = com.spyneai.shoot.repository.model.sku.Sku(
                    uuid = it.skuUuid,
                    projectUuid = it.projectUuid,
                    categoryId = it.categoryId,
                    categoryName = it.categoryName,
                    skuName = name,
                    subcategoryName = it.subCategory,
                    subcategoryId = it.categoryId,
                    threeSixtyFrames = it.frames,
                    initialFrames = it.frames,
                    totalFrames = it.frames,
                    isSelectAble = true,
                    imagePresent = 0,
                    videoPresent = 1
                )

                viewModel.sku = sku
            }

            GlobalScope.launch(Dispatchers.IO) {
                val id = viewModel.insertProject()
                Log.d(TAG, "createProject: $id")
                viewModel.insertSku()

                //start sku service sync service
                requireContext().startUploadingService(
                    ThreeSixtyProjectAndSkuDialog::class.java.simpleName,
                    ServerSyncTypes.CREATE
                )

                GlobalScope.launch(Dispatchers.Main) {
                    //notify project created
                    viewModel.isProjectCreated.value = true
                    dismiss()
                }
            }
        }
    }

//    private fun observeCreateProject() {
//        viewModel.createProjectRes.observe(viewLifecycleOwner,{
//            when(it){
//                is Resource.Success -> {
//                    requireContext().captureEvent(
//                        Events.CREATE_360_PROJECT,
//                        HashMap<String,Any?>()
//                            .apply {
//                               this.put("project_name",removeWhiteSpace(binding.etVinNumber.text.toString()))
//                            }
//                            )
//
//                    viewModel.videoDetails.apply {
//                        projectId = it.value.project_id
//                        this.skuName = sku
//                    }
//
//                    val project = Project()
//                    project.projectName = removeWhiteSpace(binding.etVinNumber.text.toString())
//                    project.createdOn = System.currentTimeMillis()
//                    project.categoryId = viewModel.videoDetails.categoryId
//                    project.categoryName = viewModel.videoDetails.categoryName
//                    project.projectId = it.value.project_id
//
//                    viewModel.insertProject(project)
//
//                    createSku(it.value.project_id,viewModel.videoDetails.type,false)
//                }
//
//                is Resource.Failure -> {
//                    requireContext().captureFailureEvent(
//                        Events.CREATE_360_PROJECT_FAILED, HashMap<String,Any?>(),
//                        it.errorMessage!!
//                    )
//                    Utilities.hideProgressDialog()
//                    handleApiError(it) { createProject(
//                        removeWhiteSpace(binding.etVinNumber.text.toString()),
//                        removeWhiteSpace(binding.etVinNumber.text.toString())
//                    )}
//                }
//            }
//        })
//    }
//
//    private fun createSku(projectId: String, prod_sub_cat_id : String,showDialog : Boolean) {
//        if (showDialog)
//            Utilities.showProgressDialog(requireContext())
//
//        this.projectId = projectId
//        this.prod_sub_cat_id = prod_sub_cat_id
//
//        viewModel.createSku(
//            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
//            projectId,
//            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
//            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
//            viewModel.videoDetails.skuName.toString()
//        )
//    }
//
//    private fun observeSku() {
//        viewModel.createSkuRes.observe(viewLifecycleOwner,{
//            when(it) {
//                is Resource.Success -> {
//                    requireContext().captureEvent(
//                        Events.CREATE_360_SKU,
//                        HashMap<String,Any?>()
//                            .apply {
//                                this.put("sku_name",viewModel.videoDetails.skuName.toString())
//                                this.put("project_id",projectId)
//                                this.put("prod_sub_cat_id",prod_sub_cat_id)
//                            }
//                    )
//
//                    Utilities.hideProgressDialog()
//
//                    viewModel.videoDetails.apply {
//                        skuId = it.value.sku_id
//                    }
//
//                    val sku = Sku()
//                    sku?.skuId = it.value.sku_id
//                    sku?.skuName = viewModel.videoDetails.skuName
//                    sku?.projectId = projectId
//                    sku?.createdOn = System.currentTimeMillis()
//                    sku?.totalImages = viewModel.videoDetails.frames
//                    sku?.categoryName = viewModel.videoDetails.categoryName
//                    sku?.categoryId = viewModel.videoDetails.categoryId
//                    sku?.subcategoryName = viewModel.videoDetails.categoryId!!
//                    sku?.subcategoryId = viewModel.videoDetails.categoryId!!
//                    sku?.threeSixtyFrames = viewModel.videoDetails.frames
//
//                    //add sku to local database
//                    viewModel.insertSku(sku!!)
//
//                    val video = VideoDetails()
//                    video?.projectId = projectId
//                    video?.skuName = viewModel.videoDetails.skuName
//                    video?.skuId = it.value.sku_id
//                    video?.type = "360_exterior"
//                    video?.categoryName = viewModel.videoDetails.categoryName
//                    video?.categoryId = viewModel.videoDetails.categoryId
//                    video?.subCategory = viewModel.videoDetails.categoryId!!
//                    video?.frames = viewModel.videoDetails.frames
//
//                    viewModel.insertVideo(video)
//
//                    //notify project created
//                    viewModel.isProjectCreated.value = true
//                    dismiss()
//                }
//
//                is Resource.Failure -> {
//                    requireContext().captureFailureEvent(Events.CREATE_360_SKU_FAILED, HashMap<String,Any?>(),
//                        it.errorMessage!!
//                    )
//                    Utilities.hideProgressDialog()
//                    handleApiError(it) {createSku(projectId,prod_sub_cat_id,true)}
//                }
//            }
//        })
//    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogCreateProjectAndSkuBinding.inflate(inflater, container, false)
}