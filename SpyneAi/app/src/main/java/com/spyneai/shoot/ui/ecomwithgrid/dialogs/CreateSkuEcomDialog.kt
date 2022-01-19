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
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.CreateProjectRes
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.utils.log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateSkuEcomDialog : BaseDialogFragment<ShootViewModel, CreateSkuEcomDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

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
                        removeWhiteSpace(binding.etSkuName.text.toString())
                    )
                }
            }
        }
    }


    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")


    private fun createSku(skuName: String) {
        val sku = Sku(
            uuid = getUuid(),
            skuName = skuName,
            projectUuid = viewModel.project?.uuid,
            projectId = viewModel.project?.projectId,
            categoryId = viewModel.categoryDetails.value?.categoryId,
            categoryName = viewModel.categoryDetails.value?.categoryName,
            subcategoryName = viewModel.subCategory.value?.sub_cat_name,
            subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id,
            initialFrames = viewModel.exterirorAngles.value
        )

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
        GlobalScope.launch {
            viewModel.insertSku()
        }
        dismiss()
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
