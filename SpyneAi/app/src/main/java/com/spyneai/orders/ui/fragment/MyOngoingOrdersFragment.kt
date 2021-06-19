package com.spyneai.orders.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.MyOngoingOrdersFragmentBinding
import com.spyneai.orders.data.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.MyOngoingOrdersAdapter

class MyOngoingOrdersFragment : BaseFragment<MyOrdersViewModel, MyOngoingOrdersFragmentBinding>(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.rvMyOngoingOrders.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            adapter = MyOngoingOrdersAdapter(requireContext())
        }
    }

    override fun getViewModel() = MyOrdersViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = MyOngoingOrdersFragmentBinding.inflate(inflater, container, false)
}