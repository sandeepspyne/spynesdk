package com.spyneai.threesixty.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentFidelitySelectionBinding
import com.spyneai.needs.AppConstants
import com.spyneai.setLocale
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.threesixty.ui.ThreeSixtyActivity

class FidelitySelectionFragment : BaseFragment<ThreeSixtyViewModel, FragmentFidelitySelectionBinding>() {

    var updateFidelity = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().setLocale()

        binding.ivFidelity.getSettings().setJavaScriptEnabled(true)

        binding.ivFidelity.loadUrl("https://www.spyne.ai/shoots/shoot?skuId=hotstone")

        setUpFramesSelection()
    }





    private fun setUpFramesSelection() {
        var frame = getString(R.string.frames)
        
        val npFrames =  arrayOf("8 "+frame, "12 "+frame, "16 "+frame, "24 "+frame, "36 "+frame, "72 "+frame)

        var lastSelectedFrames = 24
        var newSelectedFrames = 24

        if (viewModel.videoDetails?.frames != 0){
            updateFidelity = true
            lastSelectedFrames = viewModel.videoDetails?.frames!!
            newSelectedFrames = viewModel.videoDetails?.frames!!
        }

        when(lastSelectedFrames){
            8 -> binding.npFrames.minValue = 0
            12 -> binding.npFrames.minValue = 1
            16 -> binding.npFrames.minValue = 2
            24 -> binding.npFrames.minValue = 3
            36 -> binding.npFrames.minValue = 4
            72 -> binding.npFrames.minValue = 5
        }

        binding.npFrames.minValue = 0
        binding.npFrames.maxValue = npFrames.size - 1
        binding.npFrames.displayedValues = npFrames

        binding.npFrames.setOnValueChangedListener { _, _, newVal ->
            when(npFrames[newVal]) {
                "8 $frame" -> newSelectedFrames = 8
                "12 $frame" -> newSelectedFrames = 12
                "16 $frame" -> newSelectedFrames = 16
                "24 $frame" -> newSelectedFrames = 24
                "36 $frame" -> newSelectedFrames = 36
                "72 $frame" -> newSelectedFrames = 72
            }
        }

        binding.btnProceed.setOnClickListener {
            viewModel.videoDetails?.frames = newSelectedFrames

            if (updateFidelity) {
               viewModel.isFramesUpdated.value = true
               requireActivity().onBackPressed()

                viewModel.title.value = "Shoot Summary"
            }else{
                val videoDetails = viewModel.videoDetails

                Intent(requireContext(),ThreeSixtyActivity::class.java)
                    .apply {
                        putExtra(AppConstants.CATEGORY_NAME,videoDetails?.categoryName)
                        putExtra(AppConstants.CATEGORY_ID,videoDetails?.categoryId)
                        putExtra(AppConstants.EXTERIOR_ANGLES,videoDetails?.frames)
                        startActivity(this)
                    }

                requireActivity().finish()
            }
        }
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFidelitySelectionBinding.inflate(inflater,container,false)
}