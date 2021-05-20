package com.spyneai.credits.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.DownloadingActivity
import com.spyneai.credits.FeedbackActivity
import com.spyneai.dashboard.ui.dashboard.MainDashboardActivity
import com.spyneai.databinding.FragmentDownloadCompletedBinding

class DownloadCompletedFragment : Fragment() {

    private lateinit var binding: FragmentDownloadCompletedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_download_completed,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (arguments?.getString("image") != null){
            Glide.with(this)
                .load(arguments?.getString("image"))
                .into(binding.ivDonwlaodPreview)
        }

        binding.tvGoToHome.setOnClickListener {
            var dashboardIntent = Intent(requireContext(), MainDashboardActivity::class.java)
            dashboardIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(dashboardIntent)
        }

        binding.ivThumbsUp.setOnClickListener {
           if (requireActivity() != null){
               var activity = requireActivity() as DownloadingActivity
               activity.addFeedbackFragment()
           }
        }

        binding.ivThumbsDown.setOnClickListener {
           startFeedbackActivity(false)
        }
    }

    fun startFeedbackActivity(like : Boolean) {
        var feedbackIntent = Intent(requireContext(), FeedbackActivity::class.java)
        feedbackIntent.putExtra("like",like)

        startActivity(feedbackIntent)
    }
}