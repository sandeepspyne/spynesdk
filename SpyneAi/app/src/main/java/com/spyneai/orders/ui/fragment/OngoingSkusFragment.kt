package com.spyneai.orders.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentOngoingSkusBinding
import com.spyneai.shoot.data.ShootViewModel

class OngoingSkusFragment : BaseFragment<ShootViewModel, FragmentOngoingSkusBinding>() {
    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOngoingSkusBinding.inflate(inflater, container, false)

}