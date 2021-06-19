package com.spyneai.shoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentImageProcessingStartedBinding
import com.spyneai.shoot.data.ProcessViewModel


class ImageProcessingStartedFragment : BaseFragment<ProcessViewModel, FragmentImageProcessingStartedBinding>()  {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //load gif
        Glide.with(this).asGif().load(R.raw.image_processing_started)
            .into(binding.ivProcessing)

    }



    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentImageProcessingStartedBinding.inflate(inflater, container, false)
}