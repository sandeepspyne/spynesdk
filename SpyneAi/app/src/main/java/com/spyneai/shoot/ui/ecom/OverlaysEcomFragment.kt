package com.spyneai.shoot.ui.ecom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentOverlaysEcomBinding
import com.spyneai.shoot.data.ShootViewModel

class OverlaysEcomFragment : BaseFragment<ShootViewModel, FragmentOverlaysEcomBinding>() {




    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlaysEcomBinding.inflate(inflater, container, false)

}