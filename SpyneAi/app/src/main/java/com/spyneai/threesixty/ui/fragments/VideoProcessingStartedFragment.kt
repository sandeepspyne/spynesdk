package com.spyneai.threesixty.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.activity.OrderActivity
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.databinding.Fragment360ShotSummaryBinding
import com.spyneai.databinding.FragmentVideoProcessingStartedBinding
import com.spyneai.gotoHome
import com.spyneai.threesixty.data.ThreeSixtyViewModel


class VideoProcessingStartedFragment :  BaseFragment<ThreeSixtyViewModel, FragmentVideoProcessingStartedBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this).load(R.drawable.app_logo)
            .into(binding.ivProcessing)

        binding.llHome.setOnClickListener {
            requireContext().gotoHome()
        }

        binding.btnOngoingProjects.setOnClickListener {
            val intent = Intent(requireContext(), MainDashboardActivity::class.java)
            intent.putExtra("show_ongoing",true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentVideoProcessingStartedBinding.inflate(inflater, container, false)
}