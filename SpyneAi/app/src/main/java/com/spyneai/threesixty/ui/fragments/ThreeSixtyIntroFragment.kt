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
import com.spyneai.setLocale
import com.spyneai.threesixty.adapters.ThreeSixtySampleAdapter
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtyIntroFragment : BaseFragment<ThreeSixtyViewModel,Fragment360IntroBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        requireContext().setLocale()
        refreshTexts()


        binding.ivFidelity.getSettings().setJavaScriptEnabled(true)

        binding.ivFidelity.loadUrl("https://www.spyne.ai/shoots/shoot?skuId=hotstone")

        binding.btnStartClicking.setOnClickListener {
            Navigation.findNavController(binding.btnStartClicking)
                .navigate(R.id.action_threeSixtyIntroFragment_to_fidelitySelectionFragment)
            var fidelity_selection = getString(R.string.fidelity_selection)

            viewModel.title.value = fidelity_selection
        }
    }


    private fun refreshTexts() {
        binding.apply {
            tvFeatures.text = getString(R.string.features)
            tvEmbded.text = getString(R.string.embed_directly_to)
            tvShootAnywhere.text = getString(R.string.shoot_aywhere)
            tvShootAnyCar.text = getString(R.string.shoot_any_car)
            tvVideoShoot.text = getString(R.string.video_shoot)
            tvCreateHigh.text = getString(R.string.create_high_fidelity)
            btnStartClicking.text = getString(R.string.start_shoot)
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