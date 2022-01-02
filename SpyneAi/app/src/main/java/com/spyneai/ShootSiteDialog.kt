package com.spyneai

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.dashboard.ui.DashboardViewModel
import com.spyneai.databinding.FragmentShootSiteDialogBinding


class ShootSiteDialog : BaseDialogFragment<DashboardViewModel, FragmentShootSiteDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        binding.btCamera.setOnClickListener {

            viewModel.isStartAttendance.value = true
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

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentShootSiteDialogBinding.inflate(inflater, container, false)
}