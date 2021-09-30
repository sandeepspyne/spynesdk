package com.spyneai.reshoot

import android.view.LayoutInflater
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentReshootBinding
import com.spyneai.processedimages.ui.data.ProcessedViewModel

class ReshootFragment : BaseFragment<ProcessedViewModel,FragmentReshootBinding>(){

    override fun getViewModel() = ProcessedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReshootBinding.inflate(inflater, container, false)
}