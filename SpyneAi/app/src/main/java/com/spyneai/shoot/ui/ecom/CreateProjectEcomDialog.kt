package com.spyneai.shoot.ui.ecom

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.CreateProjectEcomDialogBinding
import com.spyneai.shoot.data.ShootViewModel

class CreateProjectEcomDialog : BaseDialogFragment<ShootViewModel, CreateProjectEcomDialogBinding>() {



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = CreateProjectEcomDialogBinding.inflate(inflater, container, false)

}