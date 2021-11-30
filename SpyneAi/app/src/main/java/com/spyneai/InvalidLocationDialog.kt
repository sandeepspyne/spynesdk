package com.spyneai

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.FragmentInvalidLocationDialogBinding
import com.spyneai.databinding.FragmentShootSiteDialogBinding

class InvalidLocationDialog : BaseDialogFragment<DashboardViewModel, FragmentInvalidLocationDialogBinding>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        binding.btOk.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentInvalidLocationDialogBinding.inflate(inflater, container, false)
}