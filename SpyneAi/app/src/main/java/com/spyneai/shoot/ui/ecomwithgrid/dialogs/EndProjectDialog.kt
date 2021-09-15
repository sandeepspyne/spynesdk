package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.EndProjectDialogBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.log


class EndProjectDialog : BaseDialogFragment<ShootViewModel, EndProjectDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        viewModel.getProjectDetail(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.projectId.value.toString()
        )

        viewModel.projectDetailResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {

                    binding.pbEndProject.visibility = View.GONE
                    binding.btYes.isEnabled = true

                    binding.tvProjectName.text = it.value.data.project_name
                    binding.tvTotalSkuCaptured.text = it.value.data.total_sku.toString()
                    binding.tvTotalImageCaptured.text = it.value.data.total_images.toString()

                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })



        binding.btNo.setOnClickListener {
            dismiss()
            log("end project dialog dismiss- NO")
        }

        binding.btYes.setOnClickListener {
            viewModel.updateProjectStatus(viewModel.sku.value?.projectId!!)
            viewModel.showProjectDetail.value = true
            dismiss()
            log("end project dialog dismiss- Yes")
        }

        binding.ivCloseDialog.setOnClickListener {
            dismiss()
            log("end project dialog dismiss- Image")
        }

    }

    override fun onStop() {
        super.onStop()
        log("onStop(EndProjectDialog) called")
        dismissAllowingStateLoss()
    }


    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
        dialog?.window?.setGravity(Gravity.BOTTOM)
        getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.WHITE));
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = EndProjectDialogBinding.inflate(inflater, container, false)

}