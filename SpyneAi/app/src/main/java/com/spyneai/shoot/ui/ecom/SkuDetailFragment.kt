package com.spyneai.shoot.ui.ecom

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentSkuDetailBinding
import com.spyneai.shoot.data.ShootViewModel

class SkuDetailFragment : BaseFragment<ShootViewModel, FragmentSkuDetailBinding>() {



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSkuDetailBinding.inflate(inflater, container, false)

}