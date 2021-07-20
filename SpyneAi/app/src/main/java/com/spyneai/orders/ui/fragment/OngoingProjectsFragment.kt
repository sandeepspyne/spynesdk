package com.spyneai.orders.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentOngoingOrdersBinding
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel

class OngoingProjectsFragment : BaseFragment<MyOrdersViewModel, FragmentOngoingOrdersBinding>() {



    override fun getViewModel()= MyOrdersViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOngoingOrdersBinding.inflate(inflater, container, false)
}