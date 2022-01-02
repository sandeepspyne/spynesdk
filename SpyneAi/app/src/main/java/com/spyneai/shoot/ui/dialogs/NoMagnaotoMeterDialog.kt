package com.spyneai.shoot.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.dashboard.ui.DashboardViewModel
import com.spyneai.databinding.DialogNoMagnatoMeterBinding

class NoMagnaotoMeterDialog : BaseDialogFragment<DashboardViewModel, DialogNoMagnatoMeterBinding>() {



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        binding?.tvOKay?.setOnClickListener {
            viewModel.continueAnyway.value = true
            dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
        if (dialog != null){
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }
    }


    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogNoMagnatoMeterBinding.inflate(inflater, container, false)
}