package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import com.spyneai.shoot.utils.log

class CreateSkuEcomDialog : BaseDialogFragment<ShootViewModel, CreateSkuEcomDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)
        binding.etSkuName.setText("sku" + viewModel.skuNumber.value)

        binding.ivClose.setOnClickListener {
            requireActivity().onBackPressed()

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
                        log("create sku started")
                        log("project id: " + viewModel.projectId.value.toString())
                        log("sku name: " + binding.etSkuName.text.toString())
                        createSku(
                            viewModel.projectId.value.toString(),
                            removeWhiteSpace(binding.etSkuName.text.toString())
                        )
                    }
                }
            }

            observCreateSku()
        }
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
                            HashMap<String,Any?>()
                                .apply {
                                    this.put(
                                    "sku_name",
                                    viewModel.sku.value?.skuName.toString()
                                )
                                    this.put("project_id", viewModel.sku.value?.projectId)
                                    this.put("prod_sub_cat_id", "")
                                }
                        )

                        //notify sku created
                        //notify project created
                        val sku = viewModel.sku.value
                        sku?.skuId = it.value.sku_id
                        sku?.skuName = removeWhiteSpace(binding.etSkuName.text.toString())
                        sku?.projectId = viewModel.sku.value?.projectId
                        sku?.createdOn = System.currentTimeMillis()
                        sku?.totalImages = viewModel.exterirorAngles.value
                        sku?.categoryName = viewModel.categoryDetails.value?.categoryName
                        sku?.categoryId = viewModel.categoryDetails.value?.categoryId
                        sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
                        sku?.subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id
                        sku?.exteriorAngles = viewModel.exterirorAngles.value

                        viewModel.sku.value = sku
                        viewModel.isSkuCreated.value = true
                        viewModel.showLeveler.value = true

                        //add sku to local database
                        viewModel.insertSku(sku!!)

                        //viewModel.isSubCategoryConfirmed.value = true


                        //add sku to local database
//                    viewModel.insertSku(sku!!)
                        dismiss()
                    }

                    is Resource.Failure -> {
                        Utilities.hideProgressDialog()
                        requireContext().captureFailureEvent(
                            Events.CREATE_SKU_FAILED, HashMap<String,Any?>(),
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
