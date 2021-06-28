package com.spyneai.ecom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.ConfirmReshootPortraitDialogBinding
import com.spyneai.shoot.data.ShootViewModel

class ConfirmReshootPortraitDialog : BaseFragment<ShootViewModel, ConfirmReshootPortraitDialogBinding>() {




    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = ConfirmReshootPortraitDialogBinding.inflate(inflater, container, false)

}