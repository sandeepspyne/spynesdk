package com.spyneai.shoot.ui.ecomwithgrid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.posthog.android.Properties
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentGridEcomBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.CapturedImageAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.Project
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateProjectEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateSkuEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ProjectTagDialog
import com.spyneai.shoot.utils.log
import java.util.*

class GridEcomFragment : BaseFragment<ShootViewModel, FragmentGridEcomBinding>() {


    lateinit var capturedImageAdapter: CapturedImageAdapter
    lateinit var capturedImageList: ArrayList<String>
    var position = 1


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (viewModel.projectId.value == null){
            if(Utilities.getPreference(requireContext(), AppConstants.STATUS_PROJECT_NAME).toString() =="true")
                getProjectName()
            else
                initProjectDialog()
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

        binding.ivNext.setOnClickListener {
                    InfoDialog().show(
                        requireActivity().supportFragmentManager,
                        "InfoDialog"
                    )
        }

        binding.ivEnd.setOnClickListener {
            if (viewModel.fromDrafts){
                viewModel.stopShoot.value = true
            }else {
                if (viewModel.isStopCaptureClickable)
                    viewModel.stopShoot.value = true
            }
        }

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (!it.isNullOrEmpty()) {
                    capturedImageList = ArrayList<String>()
                    position = it.size - 1
                    capturedImageList.clear()
                    for (i in 0..(it.size - 1))
                        (capturedImageList as ArrayList).add(it[i].capturedImage)
                    initCapturedImages()

                    if (viewModel.showDialog)
                        showImageConfirmDialog(it.get(it.size - 1))
                    log("call showImageConfirmDialog")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        // set sku name
        viewModel.isSkuCreated.observe(viewLifecycleOwner, {
            if (it) {
                binding.tvSkuName?.text = viewModel.sku.value?.skuName
                binding.tvSkuName.visibility = View.VISIBLE
                log("sku name set to text view: "+viewModel.sku.value?.skuName)
                viewModel.isSkuCreated.value = false

                if (!viewModel.fromDrafts)
                    viewModel.shootNumber.value = 0

                val s = ""
            }
        })

        viewModel.reshootCapturedImage.observe(viewLifecycleOwner,{
            if (it){
                capturedImageAdapter.removeLastItem()
            }
        })

        viewModel.hideLeveler.observe(viewLifecycleOwner,{
            if (viewModel.categoryDetails.value?.imageType == "Info"){
                binding.apply {
                    ivNext.visibility = View.GONE
                    ivEnd.visibility = View.VISIBLE
                }
            }
        })

        viewModel.confirmCapturedImage.observe(viewLifecycleOwner,{
            if (it){
                binding.tvImageCount.text = viewModel.shootList.value!!.size.toString()
            }
        })

    }

    private fun getProjectName(){

        viewModel.getProjectName(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())

        viewModel.getProjectNameResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {

                    Utilities.hideProgressDialog()

                    viewModel.dafault_project.value = it.value.data.dafault_project
                    viewModel.dafault_sku.value = it.value.data.dafault_sku
                    initProjectDialog()
                    log("project and SKU dialog shown")
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    log("get project name failed")
                    requireContext().captureFailureEvent(
                        Events.CREATE_PROJECT_FAILED, Properties(),
                        it.errorMessage!!
                    )

                    Utilities.hideProgressDialog()
                    handleApiError(it) { getProjectName()}
                }
            }
        })

    }


    private fun initCapturedImages() {
        capturedImageAdapter = CapturedImageAdapter(
            requireContext(),
            capturedImageList
        )

        binding.rvCapturedImages.apply {
            this?.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this?.adapter = capturedImageAdapter
        }


    }

    override fun onResume() {
        super.onResume()

        if (viewModel.fromDrafts){
            if (viewModel.showLeveler.value == null || viewModel.showLeveler.value == false){
                viewModel.showLeveler.value = true
                viewModel.showDialog = true
            }

        }
    }

    private fun initSkuDialog() {
        CreateSkuEcomDialog().show(requireFragmentManager(), "CreateSkuEcomDialog")
    }

    private fun initProjectDialog() {
        ProjectTagDialog().show(requireFragmentManager(), "CreateProjectEcomDialog")
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData

        when (viewModel.categoryDetails.value?.imageType) {
            "Info" -> {
                //CropDialog().show(requireFragmentManager(), "CropDialog")
                ConfirmReshootEcomDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
            }
            else ->
                ConfirmReshootEcomDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
        }
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGridEcomBinding.inflate(inflater, container, false)

}