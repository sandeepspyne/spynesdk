package com.spyneai.shoot.ui.ecom

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentProjectDetailBinding
import com.spyneai.shoot.data.ShootViewModel

class ProjectDetailFragment : BaseFragment<ShootViewModel, FragmentProjectDetailBinding>() {



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProjectDetailBinding.inflate(inflater, container, false)

}