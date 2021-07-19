package com.spyneai.orders.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentCompletedOrdersBinding
import com.spyneai.databinding.FragmentCompletedSkusBinding
import com.spyneai.shoot.data.ShootViewModel

class CompletedSkusFragment : BaseFragment<ShootViewModel, FragmentCompletedSkusBinding>() {





    override fun getViewModel() = ShootViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCompletedSkusBinding.inflate(inflater, container, false)


}