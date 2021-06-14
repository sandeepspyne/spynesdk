package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.DialogSubcategoryConfirmationBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.Sku

class SubCategoryConfirmationDialog : BaseDialogFragment<ShootViewModel, DialogSubcategoryConfirmationBinding>(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val subCategory =  viewModel.subCategory.value

        try {
            Glide.with(requireContext()).load(AppConstants.BASE_IMAGE_URL +
                    subCategory?.display_thumbnail)
                .into(binding.ivSubcat)

            binding.tvSubcatName.text = subCategory?.sub_cat_name

            binding.tvAnglesCount.text = viewModel.selectedAngles.value.toString()

        }catch (e : Exception){
            e.printStackTrace()
        }

        binding.btnYes.setOnClickListener {
           viewModel.createProjectRes.observe(viewLifecycleOwner,{
               when(it){
                   is Resource.Sucess -> {
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
            viewModel.sku.value.toString()
        )

        viewModel.createProjectRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Sucess -> {
                    viewModel.isSubCategoryConfirmed.value = true
                    dismiss()
                }

                is Resource.Loading -> {

                }

                is Resource.Failure -> {
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