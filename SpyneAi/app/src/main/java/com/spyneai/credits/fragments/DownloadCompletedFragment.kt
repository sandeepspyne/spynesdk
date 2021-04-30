package com.spyneai.credits.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.spyneai.R
import com.spyneai.activity.DashboardActivity
import com.spyneai.credits.FeedbackActivity
import com.spyneai.databinding.ActivityCreditFailedBinding
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


        binding.tvGoToHome.setOnClickListener {
            var dashboardIntent = Intent(requireContext(), DashboardActivity::class.java)
            dashboardIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(dashboardIntent)
        }

        binding.ivThumbsUp.setOnClickListener {  }

        binding.ivThumbsDown.setOnClickListener {
            var feedbackIntent = Intent(requireContext(), FeedbackActivity::class.java)

            startActivity(feedbackIntent)
        }

    }
}