package com.spyneai.threesixty.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.Fragment360ShotSummaryBinding
import com.spyneai.databinding.FragmentVideoProcessingStartedBinding
import com.spyneai.gotoHome
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class VideoProcessingStartedFragment :  BaseFragment<ThreeSixtyViewModel, FragmentVideoProcessingStartedBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this).asGif().load(R.raw.logo)
            .into(binding.ivProcessing)

        binding.llHome.setOnClickListener {
            requireContext().gotoHome()
        }
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentVideoProcessingStartedBinding.inflate(inflater, container, false)
}