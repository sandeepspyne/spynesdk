package com.spyneai.shoot.ui.ecomwithgrid

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentSelectMarketplaceBinding
import com.spyneai.shoot.data.ShootViewModel


class SelectMarketplaceFragment : BaseFragment<ShootViewModel, FragmentSelectMarketplaceBinding>() {




    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectMarketplaceBinding.inflate(inflater, container, false)


}