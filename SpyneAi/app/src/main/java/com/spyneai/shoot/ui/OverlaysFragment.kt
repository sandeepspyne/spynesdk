package com.spyneai.shoot.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.dashboard.ui.base.BaseFragment
import com.spyneai.databinding.FragmentOverlaysBinding
import com.spyneai.shoot.data.ShootViewModel

class OverlaysFragment : BaseFragment<ShootViewModel,FragmentOverlaysBinding>() {

    override fun getViewModel() =  ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlaysBinding.inflate(inflater, container, false)



}