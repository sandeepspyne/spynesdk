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
import com.spyneai.shoot.data.ProcessViewModel


class ImageProcessingStartedFragment : BaseFragment<ProcessViewModel, FragmentImageProcessingStartedBinding>()  {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //load gif
        Glide.with(this).asGif().load(R.raw.image_processing_started)
            .into(binding.ivProcessing)

        binding.llHome.setOnClickListener {
            requireContext().gotoHome()
        }

        if (getString(R.string.app_name) == "Karvi.com"){
            binding.llStartNewShoot.visibility = View.VISIBLE

            binding.llStartNewShoot.setOnClickListener {
                val intent = Intent(requireContext(),ShootActivity::class.java)
                intent.putExtra(AppConstants.CATEGORY_ID, AppConstants.CARS_CATEGORY_ID)
                intent.putExtra(AppConstants.CATEGORY_NAME,"Automobiles")
                startActivity(intent)

                requireActivity().finish()
            }


            binding.ivHome.visibility = View.VISIBLE
            binding.ivHome.setOnClickListener {
                requireContext().gotoHome()
            }
        }
    }



    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentImageProcessingStartedBinding.inflate(inflater, container, false)
}