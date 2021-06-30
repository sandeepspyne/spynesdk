package com.spyneai.shoot.ui.ecom

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.ConfirmReshootEcomDialogBinding
import com.spyneai.shoot.data.ShootViewModel

class ConfirmReshootEcomDialog : BaseFragment<ShootViewModel, ConfirmReshootEcomDialogBinding >() {



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = ConfirmReshootEcomDialogBinding.inflate(inflater, container, false)

}