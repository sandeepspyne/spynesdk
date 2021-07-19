package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.posthog.android.Properties
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.DialogSubcategoryConfirmationBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel

class SubCategoryConfirmationDialog : BaseDialogFragment<ShootViewModel, DialogSubcategoryConfirmationBinding>(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val subCategory =  viewModel.subCategory.value

        try {
            Glide.with(requireContext()).load(AppConstants.BASE_IMAGE_URL +
                    subCategory?.display_thumbnail)
                .into(binding.ivSubcat)

            binding.tvSubcatName.text = subCategory?.sub_cat_name

            binding.tvAngles.text = viewModel.exterirorAngles.value.toString()
        }catch (e : Exception){
            e.printStackTrace()
        }

        binding.tvCancel.setOnClickListener { dismiss() }

        binding.btnYes.setOnClickListener {
           viewModel.createProjectRes.observe(viewLifecycleOwner,{
               when(it){
                   is Resource.Success -> {
                       createSku(it.value.project_id, subCategory?.prod_sub_cat_id.toString())
                   }
                   else -> {}
               }
           })
        }
    }

    private fun createSku(projectId: String, prod_sub_cat_id : String) {
        viewModel.createSku(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),projectId,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            prod_sub_cat_id!!,
            viewModel.sku.value?.skuName.toString(),
            viewModel.exterirorAngles.value!!
        )

        viewModel.createSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name",viewModel.sku.value?.skuName.toString())
                            .putValue("project_id",projectId)
                            .putValue("prod_sub_cat_id",prod_sub_cat_id)
                            .putValue("angles",viewModel.exterirorAngles.value!!))

                    Utilities.hideProgressDialog()
                    val sku = viewModel.sku.value
                    sku?.skuId = it.value.sku_id
                    sku?.totalImages = viewModel.exterirorAngles.value

                    viewModel.sku.value = sku
                    viewModel.isSubCategoryConfirmed.value = true

                    //add sku to local database
                    viewModel.insertSku(sku!!)
                    dismiss()
                }

                is Resource.Loading ->  Utilities.showProgressDialog(requireContext())

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogSubcategoryConfirmationBinding.inflate(inflater, container, false)

}