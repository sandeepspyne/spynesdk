package com.spyneai.shoot.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentImageProcessingStartedBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.videorecording.RecordVideoActivity


class ImageProcessingStartedFragment : BaseFragment<ProcessViewModel, FragmentImageProcessingStartedBinding>()  {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //load gif
        Glide.with(this).asGif().load(R.raw.image_processing_started)
            .into(binding.ivProcessing)

        binding.llHome.setOnClickListener {
            requireContext().gotoHome()
        }

        binding.llThreeSixtyShoot.setOnClickListener {
            val intent = Intent(requireContext(), RecordVideoActivity::class.java)
            intent.putExtra("sku_id", viewModel.sku.value?.skuId!!)
            intent.putExtra("user_id", Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString())

            startActivity(intent)
        }
    }



    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentImageProcessingStartedBinding.inflate(inflater, container, false)
}