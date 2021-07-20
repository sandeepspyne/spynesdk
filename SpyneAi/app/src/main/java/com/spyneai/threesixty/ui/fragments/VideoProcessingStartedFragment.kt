package com.spyneai.threesixty.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.Fragment360ShotSummaryBinding
import com.spyneai.databinding.FragmentVideoProcessingStartedBinding
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class VideoProcessingStartedFragment :  BaseFragment<ThreeSixtyViewModel, FragmentVideoProcessingStartedBinding>() {

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentVideoProcessingStartedBinding.inflate(inflater, container, false)
}