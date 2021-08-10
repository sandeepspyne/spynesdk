package com.spyneai.orders.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.FragmentMyOrdersBinding
import com.spyneai.orders.ui.adapter.OrdersSlideAdapter

class MyOrdersFragment : BaseFragment<DashboardViewModel, FragmentMyOrdersBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        setupTabLayout()
    }

    private fun setupTabLayout() {

        binding.viewPager.apply {
            adapter = OrdersSlideAdapter(requireActivity())
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 0){
                tab.text = getString(R.string.ongoing)
            }else{
                tab.text = "Completed"
            }
        }.attach()
    }

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMyOrdersBinding.inflate(inflater, container, false)
}