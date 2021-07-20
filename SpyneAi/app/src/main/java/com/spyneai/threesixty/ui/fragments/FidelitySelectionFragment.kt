package com.spyneai.threesixty.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentFidelitySelectionBinding
import com.spyneai.needs.AppConstants
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.threesixty.ui.ThreeSixtyActivity

class FidelitySelectionFragment : BaseFragment<ThreeSixtyViewModel, FragmentFidelitySelectionBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFramesSelection()


    }

    private fun setUpFramesSelection() {
        val npFrames =  arrayOf("8 Frames", "12 Frames", "16 Frames", "24 Frames", "36 Frames", "72 Frames")

        var lastSelectedFrames = 24
        var newSelectedFrames = 24

        if (viewModel.videoDetails.frames != 0){
            lastSelectedFrames = viewModel.videoDetails.frames
            newSelectedFrames = viewModel.videoDetails.frames
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
                "8 Frames" -> newSelectedFrames = 8
                "12 Frames" -> newSelectedFrames = 12
                "16 Frames" -> newSelectedFrames = 16
                "24 Frames" -> newSelectedFrames = 24
                "36 Frames" -> newSelectedFrames = 36
                "72 Frames" -> newSelectedFrames = 72
            }
        }

        binding.btnProceed.setOnClickListener {
            viewModel.videoDetails.frames = newSelectedFrames

            val videoDetails = viewModel.videoDetails

            Intent(requireContext(),ThreeSixtyActivity::class.java)
                .apply {
                    putExtra(AppConstants.CATEGORY_NAME,videoDetails.categoryName)
                    putExtra(AppConstants.CATEGORY_ID,videoDetails.categoryId)
                    putExtra("frames",videoDetails.frames)
                    startActivity(this)
                }


        }


    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFidelitySelectionBinding.inflate(inflater,container,false)
}