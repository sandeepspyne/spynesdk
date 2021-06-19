package com.spyneai.orders.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentMyOrdersBinding
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.OrdersSlideAdapter

class MyOrdersFragment : BaseFragment<MyOrdersViewModel, FragmentMyOrdersBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayout()
    }

    private fun setupTabLayout() {

        binding.viewPager.apply {
            adapter = OrdersSlideAdapter(requireActivity())
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 0){
                tab.text = "OnGoing"
            }else{
                tab.text = "Completed"
            }
        }.attach()
    }

    override fun getViewModel() = MyOrdersViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMyOrdersBinding.inflate(inflater, container, false)
}