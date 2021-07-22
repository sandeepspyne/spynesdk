package com.spyneai.threesixty.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.Fragment360SampleBinding
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtySampleFragment : BaseFragment<ThreeSixtyViewModel,Fragment360SampleBinding>(){

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = Fragment360SampleBinding.inflate(inflater,container,false)
}