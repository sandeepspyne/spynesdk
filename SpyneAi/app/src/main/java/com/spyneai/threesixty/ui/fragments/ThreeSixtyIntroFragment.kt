package com.spyneai.threesixty.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.Fragment360IntroBinding
import com.spyneai.threesixty.adapters.ThreeSixtySampleAdapter
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtyIntroFragment : BaseFragment<ThreeSixtyViewModel,Fragment360IntroBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivFidelity.getSettings().setJavaScriptEnabled(true)

        binding.ivFidelity.loadUrl("https://www.spyne.ai/shoots/shoot?skuId=hotstone")

        binding.btnStartClicking.setOnClickListener {
            Navigation.findNavController(binding.btnStartClicking)
                .navigate(R.id.action_threeSixtyIntroFragment_to_fidelitySelectionFragment)

            viewModel.title.value = "Fidelity Selection"
        }
    }

//    private fun setupTabLayout() {
//
//        binding.viewPager.apply {
//            adapter = ThreeSixtySampleAdapter(requireActivity())
//        }
//
//        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
//            when(position) {
//                0 -> tab.text = "Hatchback"
//                1 -> tab.text = "SUV"
//                2 -> tab.text = "Sedan"
//            }
//        }.attach()
//    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = Fragment360IntroBinding.inflate(inflater,container,false)
}