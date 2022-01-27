package com.spyneai.orders.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.ui.DashboardViewModel
import com.spyneai.databinding.FragmentMyOrdersBinding
import com.spyneai.orders.ui.adapter.OrdersSlideAdapter
import com.spyneai.setLocale

@ExperimentalPagingApi
class MyOrdersFragment : BaseFragment<DashboardViewModel, FragmentMyOrdersBinding>() {

    private var TAG = "MyOrdersFragment"
    var tabId = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().setLocale()


        try {
            tabId = requireActivity().intent.extras?.get("TAB_ID") as Int
        }catch (e : Exception){

        }

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
            when (position) {
                0 -> tab.text = getString(R.string.drafts)
                1 -> tab.text = getString(R.string.ongoing)
                else -> tab.text = getString(R.string.completed)
            }
        }.attach()

        binding.tabLayout.getTabAt(tabId)?.select()
    }

    override fun getViewModel() = DashboardViewModel::class.java


    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMyOrdersBinding.inflate(inflater, container, false)
}