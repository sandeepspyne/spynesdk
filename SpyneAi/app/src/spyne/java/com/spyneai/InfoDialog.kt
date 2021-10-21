package com.spyneai

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.posthog.android.Properties
import com.spyneai.base.BaseDialogFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.DialogLogoutBinding
import com.spyneai.databinding.FragmentInfoDialogBinding
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel


class InfoDialog :  BaseDialogFragment<ShootViewModel, FragmentInfoDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llEndShoot.setOnClickListener {
            if (viewModel.fromDrafts){
                viewModel.stopShoot.value = true
            }else {
                if (viewModel.isStopCaptureClickable)
                    viewModel.stopShoot.value = true
            }

            dismiss()
        }

        binding.llShootInfo.setOnClickListener {
            viewModel.categoryDetails.value?.imageType = "Info"
            viewModel.hideLeveler.value = true
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentInfoDialogBinding.inflate(inflater, container, false)
}