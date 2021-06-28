package com.spyneai.ecom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.CreateFootwearProjectDialogBinding
import com.spyneai.databinding.DialogCreateProjectAndSkuBinding
import com.spyneai.shoot.data.ShootViewModel

class CreateFootwearProjectDialog : BaseDialogFragment<ShootViewModel, CreateFootwearProjectDialogBinding>() {



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = CreateFootwearProjectDialogBinding.inflate(inflater, container, false)

}