package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.CreateSkuEcomDialogBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.CreateProjectRes
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.utils.log

class CreateSkuEcomDialog : BaseDialogFragment<ShootViewModel, CreateSkuEcomDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        val sku = Sku()
        sku.projectId = viewModel.projectId.value
        viewModel.sku = sku

        viewModel._createProjectRes.value = Resource.Success(
            CreateProjectRes(
                "",
                sku.projectId!!,
                200
            )
        )


        binding.ivBarCode.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
            options.setPrompt("Scan a barcode")
            options.setCameraId(0) // Use a specific camera of the device
            options.setBeepEnabled(true)
            options.setOrientationLocked(false)
            barcodeLauncher.launch(options)
        }

        binding.ivClose.setOnClickListener {
            requireActivity().onBackPressed()
        }

        getProjectName()

        binding.btnProceed.setOnClickListener {
            when {
                binding.etSkuName.text.toString().isEmpty() -> {
                    binding.etSkuName.error = "Please enter product name"
                }
                binding.etSkuName.text.toString()
                    .contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) -> {
                    binding.etSkuName.error = "Special characters not allowed"
                }
                else -> {
                    createSku(
                        viewModel.projectId.value.toString(),
                        removeWhiteSpace(binding.etSkuName.text.toString())
                    )
                }
            }
        }

        observCreateSku()
    }

    private fun getProjectName() {
        viewModel.getProjectName(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString()
        )

        viewModel.getProjectNameResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {

                    Utilities.hideProgressDialog()

                    viewModel.dafault_project.value = viewModel.projectId.value
                    viewModel.dafault_sku.value = it.value.data.dafault_sku
                    binding.etSkuName.setText(it.value.data.dafault_sku)
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    log("get project name failed")
                    requireContext().captureFailureEvent(
                        Events.CREATE_PROJECT_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )

                    Utilities.hideProgressDialog()
                    handleApiError(it) { getProjectName() }
                }
            }
        })

    }


    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")


    private fun createSku(projectId: String, skuName: String) {
        Utilities.showProgressDialog(requireContext())

        viewModel.createSku(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            projectId,
            Utilities.getPreference(requireContext(), AppConstants.CATEGORY_ID).toString(),
            "",
            skuName,
            0
        )
    }

    private fun observCreateSku() {
        viewModel.createSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    requireContext().captureEvent(
                        Events.CREATE_SKU,
                        HashMap<String, Any?>()
                            .apply {
                                this.put(
                                    "sku_name",
                                    viewModel.sku?.skuName.toString()
                                )
                                this.put("project_id", viewModel.sku?.projectId)
                                this.put("prod_sub_cat_id", "")
                            }
                    )

                    //notify sku created
                    //notify project created
                    val sku = viewModel.sku
                    sku?.skuId = it.value.sku_id
                    sku?.skuName = removeWhiteSpace(binding.etSkuName.text.toString())
                    sku?.projectId = viewModel.sku?.projectId
//                    sku?.createdOn = System.currentTimeMillis()
//                    sku?.totalImages = viewModel.exterirorAngles.value
                    sku?.categoryName = viewModel.categoryDetails.value?.categoryName
                    sku?.categoryId = viewModel.categoryDetails.value?.categoryId
                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
                    sku?.subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id
                   // sku?.exteriorAngles = viewModel.exterirorAngles.value

                    viewModel.sku = sku

                    //notify project created
                    viewModel.isProjectCreated.value = true
                    viewModel.isSkuCreated.value = true


                    when (viewModel.categoryDetails.value?.categoryId) {
                        AppConstants.FOOTWEAR_CATEGORY_ID,
                        AppConstants.MENS_FASHION_CATEGORY_ID,
                        AppConstants.WOMENS_FASHION_CATEGORY_ID,
                        AppConstants.CAPS_CATEGORY_ID,
                        AppConstants.FASHION_CATEGORY_ID,
                        AppConstants.ACCESSORIES_CATEGORY_ID,
                        AppConstants.HEALTH_AND_BEAUTY_CATEGORY_ID -> {
                            viewModel.getSubCategories.value = true
                        }
                        else -> {
//                            viewModel.showLeveler.value = true
                            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                        }
                    }

                    //add sku to local database
                    viewModel.insertSku(sku!!)
                    dismiss()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    requireContext().captureFailureEvent(
                        Events.CREATE_SKU_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )
                    handleApiError(it) {
                        createSku(
                            viewModel.projectId.value.toString(),
                            removeWhiteSpace(binding.etSkuName.text.toString())
                        )
                    }
                }
            }
        })
    }


    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
        } else {

            binding.etSkuName.setText(result.contents)
        }
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
    ) = CreateSkuEcomDialogBinding.inflate(inflater, container, false)

}
