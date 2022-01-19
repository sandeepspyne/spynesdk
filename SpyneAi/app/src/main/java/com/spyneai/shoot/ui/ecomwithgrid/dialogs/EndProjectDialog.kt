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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class EndProjectDialog : BaseDialogFragment<ShootViewModel, EndProjectDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        viewModel.project?.let {
            binding.apply {
                pbEndProject.visibility = View.GONE
                btYes.isEnabled = true

                tvProjectName.text = it.projectName
                tvTotalSkuCaptured.text = it.skuCount.toString()
                tvTotalImageCaptured.text = it.imagesCount.toString()
            }
        }

        binding.btNo.setOnClickListener {
            dismiss()
            log("end project dialog dismiss- NO")
        }

        binding.btYes.setOnClickListener {
            viewModel.updateProjectStatus()
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