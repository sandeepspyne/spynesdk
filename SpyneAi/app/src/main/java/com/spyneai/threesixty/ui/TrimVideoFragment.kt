package com.spyneai.threesixty.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentRecordVideoBinding
import com.spyneai.databinding.FragmentTrimVideoBinding
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class TrimVideoFragment : BaseFragment<ThreeSixtyViewModel,FragmentTrimVideoBinding>() {

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTrimVideoBinding.inflate(inflater, container, false)
}