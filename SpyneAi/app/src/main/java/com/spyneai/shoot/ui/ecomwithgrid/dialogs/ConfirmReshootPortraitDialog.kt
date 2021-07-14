package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.ConfirmReshootPortraitDialogBinding
import com.spyneai.shoot.data.ShootViewModel

class ConfirmReshootPortraitDialog : BaseDialogFragment<ShootViewModel, ConfirmReshootPortraitDialogBinding>() {





    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT);
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = ConfirmReshootPortraitDialogBinding.inflate(inflater, container, false)

}