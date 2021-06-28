package com.spyneai.ecom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentSelectBackgroundBinding
import com.spyneai.databinding.FragmentSelectMarketplaceBinding
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.data.ShootViewModel


class SelectMarketplaceFragment : BaseFragment<ShootViewModel, FragmentSelectMarketplaceBinding>() {




    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectMarketplaceBinding.inflate(inflater, container, false)


}