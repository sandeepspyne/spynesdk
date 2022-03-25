package com.spyneai.shoot.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSelectSubcategoryAndAngleBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.sdk.Spyne
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shoot.adapters.SubcatAndAngleAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.repository.model.project.ProjectBody
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SubCategoryAndAngleFragment :
    BaseFragment<ShootViewModel, FragmentSelectSubcategoryAndAngleBinding>(),
    OnItemClickListener {


    var subcatAndAngleAdapter: SubcatAndAngleAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        viewModel.getSubCategories.observe(viewLifecycleOwner, {
//            getSubcategories()
//        })
//
//        observeSubcategories()


        when (viewModel.categoryDetails.value?.categoryId) {
            AppConstants.FOOTWEAR_CATEGORY_ID -> binding.tvDescription.text =
                getString(R.string.footwear_subcategory)
            AppConstants.BIKES_CATEGORY_ID -> binding.tvDescription.text =
                getString(R.string.bikes_subcategory)
            else -> {
                binding.tvDescription.text = "Select your ${viewModel.categoryDetails.value?.categoryName}'s Sub-categories"
            }
        }

        if (viewModel.isSkuCreated.value == null || (viewModel.categoryDetails.value?.categoryId != AppConstants.CARS_CATEGORY_ID
                    &&
                    viewModel.categoryDetails.value?.categoryId != AppConstants.BIKES_CATEGORY_ID)
        ) {
            viewModel.getSubCategories.observe(viewLifecycleOwner, {
                getSubcategories()
            })

            observeSubcategories()
        } else {
            hideViews()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.isSubcategoriesSelectionShown = true
    }

    private fun getSubcategories() {
        binding.shimmer.startShimmer()

        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )
    }

    fun observeSubcategories() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.GET_SUBCATEGORIES,
                        HashMap<String, Any?>()
                    )

                    binding.apply {
                        shimmer.stopShimmer()
                        shimmer.visibility = View.INVISIBLE
                    }

                    subcatAndAngleAdapter = SubcatAndAngleAdapter(it.value.data, this)

                    val lyManager =
                        if (requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        else
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )

                    binding.rv.apply {
                        layoutManager = lyManager
                        adapter = subcatAndAngleAdapter
                    }
                }
                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_SUBCATRGORIES_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )
                    binding.shimmer.stopShimmer()

                    handleApiError(it) { getSubcategories() }
                }
            }
        })
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectSubcategoryAndAngleBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is NewSubCatResponse.Subcategory -> {

                hideViews()

                viewModel.subCategory.value = data
                viewModel.subCatName.value = data.sub_cat_name

                when (getString(R.string.app_name)) {
                    AppConstants.SPYNE_AI, AppConstants.SPYNE_AI_AUTOMOBILE -> {
                        when (viewModel.categoryDetails.value?.categoryName) {
                            "Automobiles", "Bikes" -> {
                                selectAngles()
                            }
                            else -> {
                                viewModel.exterirorAngles.value = 0

                                updateSku()
                            }
                        }
                    }
                    AppConstants.KARVI,AppConstants.AUTO_FOTO, AppConstants.CARS24, AppConstants.CARS24_INDIA -> {
                        viewModel.exterirorAngles.value = 8
                        //create sku
                        if (viewModel.fromVideo) {
                            updateSku()
                        } else {
                            createSku()
                        }
                    }

                    AppConstants.AMAZON -> {
                        viewModel.exterirorAngles.value = 0

                        updateSku()
                    }
                    else -> selectAngles()
                }

            }
        }
    }


    private fun updateSku() {
        setSubcategoryData()

        viewModel.isSubCategoryConfirmed.value = true
        viewModel.isSkuCreated.value = true
        viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
        viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
        viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

        //add sku to local database
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.updateSubcategory()
        }
    }

    private fun hideViews() {
        binding.clRoot.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.transparent
            )
        )

        binding.apply {
            shimmer.stopShimmer()
            shimmer.visibility = View.INVISIBLE
            ivArrow?.visibility = View.GONE
            tvDescription.visibility = View.INVISIBLE
            rv.visibility = View.INVISIBLE
        }
    }

    private fun selectAngles() {
        if (Utilities.getBool(requireContext(),AppConstants.FROM_SDK,false)){
            viewModel.exterirorAngles.value = 8
            createSku()
        }else {
            setSubcategoryData()
            AngleSelectionDialog().show(
                requireActivity().supportFragmentManager,
                "AngleSelectionDialog"
            )
        }
    }

    private fun setSubcategoryData(){
        viewModel.sku?.apply {
            subcategoryName = viewModel.subCategory.value?.sub_cat_name
            subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id
            initialFrames = viewModel.exterirorAngles.value
            totalFrames = viewModel.exterirorAngles.value
        }
    }

    private fun createSku() {
        setSubcategoryData()

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
                }
            }


        }
    }

    private fun observeCreateProject(projectBody: ProjectBody) {
        viewModel.createProjectRes.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Loading -> Utilities.showProgressDialog(requireContext())

                is Resource.Success -> {
                    GlobalScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(BaseApplication.getContext())
                        val projectId = it.value.data.projectId
                        db.projectDao().updateProjectServerId(projectBody.projectData.localId,projectId)

                        if (it.value.data.draftAvailable){
                            db.shootDao().updateSkuAndImageIds(projectId,it.value.data.draftData[0].localId,it.value.data.draftData[0].skuId)
                        }else {
                            it.value.data.skusList.forEachIndexed { index, skus ->
                                db.shootDao().updateSkuAndImageIds(projectId,skus.localId,skus.skuId)
                            }
                        }

                        GlobalScope.launch(Dispatchers.Main) {
                            Utilities.hideProgressDialog()
                            viewModel.project?.projectId = it.value.data.projectId
                            if (it.value.data.draftAvailable){
                                viewModel.sku?.skuId = it.value.data.draftData[0].skuId
                                viewModel.sku?.uuid = it.value.data.draftData[0].localId
                            }
                            else
                                viewModel.sku?.skuId = it.value.data.skusList[0].skuId

                            viewModel.isSubCategoryConfirmed.value = true
                            viewModel.isSkuCreated.value = true
                            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
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

}