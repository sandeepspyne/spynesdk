package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.posthog.android.Properties
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
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.utils.log

class CreateSkuEcomDialog : BaseDialogFragment<ShootViewModel, CreateSkuEcomDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.btnProceed.setOnClickListener {
            when {
                binding.etSkuName.text.toString().isEmpty() -> {
                    binding.etSkuName.error = "Please enter product name"
                }
                binding.etSkuName.text.toString().contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) -> {
                    binding.etSkuName.error = "Special characters not allowed"
                }
                else -> {
                    log("create sku started")
                    log("project id: "+viewModel.projectId.value.toString())
                    log("sku name: "+binding.etSkuName.text.toString())
                    createSku(
                        viewModel.projectId.value.toString(), removeWhiteSpace(binding.etSkuName.text.toString())
                    )
                }
            }
        }
    }

    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")


    private fun createSku(projectId: String, skuName: String) {

        viewModel.createSku(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            projectId,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            "",
            skuName,
            0
        )

        viewModel.createSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    requireContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name", viewModel.sku.value?.skuName.toString())
                            .putValue("project_id", projectId)
                            .putValue("prod_sub_cat_id", "")
                    )

                    //notify sku created
                    val sku = Sku()
                    log("sku id created sucess")
                    log("sku id: "+it.value.sku_id)
                    sku?.skuId = it.value.sku_id
                    sku?.projectId = projectId
                    sku?.skuName = skuName

                    log("sssskkkkuuu: "+skuName)

                    viewModel.sku.value = sku

                    log("sssskkkkuuu: "+viewModel.sku.value?.skuName)

                    //viewModel.isSubCategoryConfirmed.value = true

                    viewModel.isSkuCreated.value = true

                    //add sku to local database
//                    viewModel.insertSku(sku!!)
                    dismiss()
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    requireContext().captureFailureEvent(
                        Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    handleApiError(it)
                }
            }
        })
    }



    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT);
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = CreateSkuEcomDialogBinding.inflate(inflater, container, false)

}