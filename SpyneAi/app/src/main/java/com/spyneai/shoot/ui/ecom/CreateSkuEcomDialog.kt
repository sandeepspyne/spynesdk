package com.spyneai.shoot.ui.ecom

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.CreateSkuEcomDialogBinding
import com.spyneai.shoot.data.ShootViewModel

class CreateSkuEcomDialog : BaseFragment<ShootViewModel, CreateSkuEcomDialogBinding>() {




    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = CreateSkuEcomDialogBinding.inflate(inflater, container, false)

}