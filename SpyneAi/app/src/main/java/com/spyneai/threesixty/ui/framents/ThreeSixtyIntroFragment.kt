package com.spyneai.threesixty.ui.framents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.Fragment360IntroBinding
import com.spyneai.orders.ui.adapter.OrdersSlideAdapter
import com.spyneai.threesixty.adapters.ThreeSixtySampleAdapter
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtyIntroFragment : BaseFragment<ThreeSixtyViewModel,Fragment360IntroBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayout()
    }

    private fun setupTabLayout() {

        binding.viewPager.apply {
            adapter = ThreeSixtySampleAdapter(requireActivity())
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position) {
                0 -> "Hatchback"
                1 -> "SUV"
                2 -> "Sedan"
            }
        }.attach()
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = Fragment360IntroBinding.inflate(inflater,container,false)
}