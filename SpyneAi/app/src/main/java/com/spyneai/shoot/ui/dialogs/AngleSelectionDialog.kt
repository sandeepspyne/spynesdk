package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.*
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.DialogAngleSelectionBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.sdk.Spyne
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.repository.model.project.ProjectBody
import com.spyneai.shoot.utils.shoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AngleSelectionDialog : BaseDialogFragment<ShootViewModel, DialogAngleSelectionBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false
        refreshTexts()

        showOptions()
    }

    private fun showOptions() {
        val angles: String = getString(R.string.angles)
        val valuesShoots = when (getString(R.string.app_name)) {
            AppConstants.CARS24_INDIA, AppConstants.CARS24 -> arrayOf("5 " + angles)
            AppConstants.SELL_ANY_CAR -> arrayOf("4 " + angles, "36 " + angles)
            AppConstants.SPYNE_AI, AppConstants.SPYNE_AI_AUTOMOBILE -> {
                if (viewModel.categoryDetails.value?.categoryId == AppConstants.CARS_CATEGORY_ID) {
                    arrayOf(
                        "8 " + angles,
                        "12 " + angles,
                        "16 " + angles,
                        "24 " + angles,
                        "36 " + angles
                    )
                } else {
                    arrayOf("4 " + angles, "8 " + angles, "12 " + angles)
                }
            }
            else -> arrayOf(
                "8 " + angles,
                "12 " + angles,
                "16 " + angles,
                "24 " + angles,
                "36 " + angles
            )
        }

        var newSelectedAngles = viewModel.getSelectedAngles(getString(R.string.app_name))

        when (getString(R.string.app_name)) {
            AppConstants.SELL_ANY_CAR -> {
                when (viewModel.getSelectedAngles(getString(R.string.app_name))) {
                    4 -> binding.npShoots.minValue = 0
                    36 -> binding.npShoots.minValue = 1
                }
            }
            AppConstants.SPYNE_AI,AppConstants.SPYNE_AI_AUTOMOBILE -> {
                if (viewModel.categoryDetails.value?.categoryId == AppConstants.BIKES_CATEGORY_ID){
                    when (viewModel.getSelectedAngles(getString(R.string.app_name))) {
                        4 -> binding.npShoots.minValue = 0
                        8 -> binding.npShoots.minValue = 1
                        12 -> binding.npShoots.minValue = 2
                    }
                }else {
                    when (viewModel.getSelectedAngles(getString(R.string.app_name))) {
                        8, 5 -> binding.npShoots.minValue = 0
                        12 -> binding.npShoots.minValue = 1
                        16 -> binding.npShoots.minValue = 2
                        24 -> binding.npShoots.minValue = 3
                        36 -> binding.npShoots.minValue = 4
                    }
                }
            }
            else -> {
                when (viewModel.getSelectedAngles(getString(R.string.app_name))) {
                    8, 5 -> binding.npShoots.minValue = 0
                    12 -> binding.npShoots.minValue = 1
                    16 -> binding.npShoots.minValue = 2
                    24 -> binding.npShoots.minValue = 3
                    36 -> binding.npShoots.minValue = 4
                }
            }
        }

        binding.npShoots.minValue = 0
        binding.npShoots.maxValue = valuesShoots.size - 1
        binding.npShoots.displayedValues = valuesShoots

        binding.npShoots.setOnValueChangedListener { _, _, newVal ->
            when (valuesShoots[newVal]) {
                "4 " + angles -> newSelectedAngles = 4
                "5 " + angles -> newSelectedAngles = 5
                "8 " + angles -> newSelectedAngles = 8
                "12 " + angles -> newSelectedAngles = 12
                "16 " + angles -> newSelectedAngles = 16
                "24 " + angles -> newSelectedAngles = 24
                "36 " + angles -> newSelectedAngles = 36
            }
        }
        binding.tvProceed.setOnClickListener {
            viewModel.exterirorAngles.value = newSelectedAngles

            //create sku
           // val createProjectRes = (viewModel.createProjectRes.value as Resource.Success).value

            if (viewModel.fromVideo) {
                updateSku()
            } else {
                createSku()
            }
        }
    }

    private fun refreshTexts() {
        requireContext().setLocale()
        binding.tvChooseShot.text = getString(R.string.choose_shoots)
        binding.tvMoreAngle.text = getString(R.string.more_angles)
        binding.tvProceed.text = getString(R.string.proceed)
    }


    private fun updateSku() {
        viewModel.sku?.apply {
            subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id
            subcategoryName = viewModel.subCategory.value?.sub_cat_name
            initialFrames = viewModel.exterirorAngles.value
        }

        GlobalScope.launch(Dispatchers.IO) {
            viewModel.updateVideoSkuLocally()

            GlobalScope.launch(Dispatchers.Main) {
                viewModel.isSubCategoryConfirmed.value = true
                viewModel.isSkuCreated.value = true
//                    viewModel.showLeveler.value = true
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

                requireContext().startUploadingService(
                    AngleSelectionDialog::class.java.simpleName,
                    ServerSyncTypes.CREATE
                )

                dismiss()
            }
        }
    }

    private fun createSku() {
        viewModel.sku?.apply {
            initialFrames = viewModel.exterirorAngles.value
            totalFrames = viewModel.exterirorAngles.value
        }
        //add sku to local database
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.updateSubcategory()

            //start sync service
            GlobalScope.launch(Dispatchers.Main) {
                if (Utilities.getBool(requireContext(),AppConstants.FROM_SDK,false)){
                    //create project and sku
                    val projectData = ProjectBody.ProjectData(
                        categoryId = Utilities.getPreference(requireContext(),AppConstants.CATEGORY_ID).toString(),
                        localId = viewModel.project?.uuid!!,
                        projectId = viewModel.project?.projectId,
                        projectName = viewModel.project?.projectName!!,
                        foreignSkuId = Spyne.foreignSkuId
                    )

                    val sku = viewModel.sku

                    val skuData = ProjectBody.SkuData(
                        skuId = sku?.skuId,
                        localId = sku?.uuid!!,
                        skuName = sku?.skuName!!,
                        prodCatId = sku.categoryId!!,
                        prodSubCatId = sku.subcategoryId,
                        initialNo = sku.initialFrames!!,
                        totalFramesNo = sku.totalFrames!!,
                        imagePresent = sku.imagePresent,
                        videoPresent = sku.videoPresent
                    )

                    val projectBody = ProjectBody(
                        projectData = projectData,
                        skuData = ArrayList<ProjectBody.SkuData>().apply {
                            add(skuData)
                        }
                    )
                    createProjectAndSkuOnServer(projectBody)
                    observeCreateProject(projectBody)
                }else {
                    viewModel.isSubCategoryConfirmed.value = true
                    viewModel.isSkuCreated.value = true
                    viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                    viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                    viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
                    requireContext().startUploadingService(
                        AngleSelectionDialog::class.java.simpleName,
                        ServerSyncTypes.CREATE
                    )

                    dismiss()
                }
            }


        }


//        viewModel.createSku(
//            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
//            projectId,
//            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
//            prod_sub_cat_id!!,
//            viewModel.sku?.skuName.toString(),
//            viewModel.exterirorAngles.value!!
//        )
//
//        viewModel.createSkuRes.observe(viewLifecycleOwner, {
//            when (it) {
//                is Resource.Success -> {
//                    Utilities.hideProgressDialog()
//
//                    val items = HashMap<String, Any?>()
//                    items["sku_name"] = viewModel.sku?.skuName.toString()
//                    items.put("project_id", projectId)
//                    items.put("prod_sub_cat_id", prod_sub_cat_id)
//                    items.put("angles", viewModel.exterirorAngles.value!!)
//
//                    BaseApplication.getContext().captureEvent(
//                        Events.CREATE_SKU,
//                        items
//                    )
//
//                    val sku = viewModel.sku
//                    sku?.skuId = it.value.sku_id
//                    sku?.projectId = projectId
//                    sku?.createdOn = System.currentTimeMillis()
//                    sku?.totalImages = viewModel.exterirorAngles.value
//                    sku?.categoryName = viewModel.categoryDetails.value?.categoryName
//                    sku?.categoryId = viewModel.categoryDetails.value?.categoryId
//                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
//                    sku?.subcategoryId = prod_sub_cat_id
//                    sku?.exteriorAngles = viewModel.exterirorAngles.value
//
//                    viewModel.sku = sku
//                    viewModel.isSubCategoryConfirmed.value = true
//                    viewModel.isSkuCreated.value = true
//                    viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
//                    viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
//                    viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
//
//                    //add sku to local database
//                    viewModel.insertSku(sku!!)
//
//                    val s = ""
//                    dismiss()
//                }
//
//
//                is Resource.Failure -> {
//                    viewModel.isCameraButtonClickable = true
//                    BaseApplication.getContext().captureFailureEvent(
//                        Events.CREATE_SKU_FAILED, HashMap<String, Any?>(),
//                        it.errorMessage!!
//                    )
//                    Utilities.hideProgressDialog()
//                    handleApiError(it) { createSku(projectId, prod_sub_cat_id) }
//                }
//            }
//        })
    }

    private fun observeCreateProject(projectBody: ProjectBody) {
        viewModel.createProjectRes.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Loading -> Utilities.showProgressDialog(requireContext())

                is Resource.Success -> {

                    GlobalScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(BaseApplication.getContext())
                        val projectId = it.value.data.projectId

                        val update = db.projectDao().updateProjectServerId(projectBody.projectData.localId,projectId)


                        it.value.data.skusList.forEachIndexed { index, skus ->
                            val ss = db.shootDao().updateSkuAndImageIds(projectId,skus.localId,skus.skuId)
                        }

                        GlobalScope.launch(Dispatchers.Main) {
                            Utilities.hideProgressDialog()
                            viewModel.project?.projectId = it.value.data.projectId
                            viewModel.sku?.skuId = it.value.data.skusList[0].skuId

                            viewModel.isSubCategoryConfirmed.value = true
                            viewModel.isSkuCreated.value = true
                            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
                            dismiss()
                        }
                    }

                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it){createProjectAndSkuOnServer(projectBody) }
                }
            }
        })
    }

    private fun createProjectAndSkuOnServer(projectBody: ProjectBody) {
        viewModel.createProject(projectBody)
    }

    override fun onStop() {
        super.onStop()
        dismissAllowingStateLoss()
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogAngleSelectionBinding.inflate(inflater, container, false)
}