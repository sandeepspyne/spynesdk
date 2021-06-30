package com.spyneai.shoot.ui.ecom

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.EndProjectDialogBinding
import com.spyneai.shoot.data.ShootViewModel


class EndProjectDialog : BaseFragment<ShootViewModel, EndProjectDialogBinding>() {


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = EndProjectDialogBinding.inflate(inflater, container, false)

}