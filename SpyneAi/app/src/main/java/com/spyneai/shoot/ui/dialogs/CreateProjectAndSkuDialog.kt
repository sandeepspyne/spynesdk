package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.DialogCreateProjectAndSkuBinding
import com.spyneai.shoot.data.ShootViewModel

class CreateProjectAndSkuDialog : BaseDialogFragment<ShootViewModel,DialogCreateProjectAndSkuBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmit.setOnClickListener {
            when {
                binding.etProjectName.text.toString().isEmpty() -> binding.etProjectName.error = "Please enter project name"
                binding.etVinNumber.text.toString().isEmpty() -> {
                    binding.etVinNumber.error = "Please enter any unique number"
                }
                else -> {
                    createProject(binding.etProjectName.text.toString())
                }
            }
        }
    }

    private fun createProject(projectName : String) {
        viewModel.createProject("3c436435-238a-4bdc-adb8-d6182fddeb43",projectName,"cat_d8R14zUNE")

        viewModel.createProjectRes.observe(viewLifecycleOwner,{
            when(it){
                    is Resource.Sucess -> {
                        //create sku
                        createSku(it.value.project_id)
                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Failure -> {
                        handleApiError(it)
                    }
            }
        })

    }

    private fun createSku(projectId: String) {
        viewModel.createSku("3c436435-238a-4bdc-adb8-d6182fddeb43",projectId,
        "cat_d8R14zUNE","prod_seY3vxhATCH",binding.etVinNumber.text.toString())

        viewModel.createProjectRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Sucess -> {
                    //notify sku created
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
    ) = DialogCreateProjectAndSkuBinding.inflate(inflater, container, false)
}