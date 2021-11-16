package com.spyneai.credits.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.spyneai.activity.DownloadingActivity
import com.spyneai.credits.FeedbackActivity
import com.spyneai.databinding.FragmentDownloadCompletedBinding
import com.spyneai.gotoHome

class DownloadCompletedFragment : Fragment() {

    private var _binding: FragmentDownloadCompletedBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDownloadCompletedBinding.inflate(inflater, container, false)

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
            requireContext().gotoHome()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}