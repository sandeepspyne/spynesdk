package com.spyneai.dashboard.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.posthog.android.Properties
import com.spyneai.captureEvent
import com.spyneai.databinding.LogoutDialogBinding
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events


class LogoutDashBoardFragment : Fragment() {


    private var _binding : LogoutDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = LogoutDialogBinding.inflate(inflater, container, false)

        if (Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL).toString() != ""){
            binding.tvUserEmail.setText("("+Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL)+")")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.llLogout.setOnClickListener {
            requireContext().captureEvent(Events.LOG_OUT, Properties())

            Utilities.savePrefrence(requireContext(), AppConstants.TOKEN_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.AUTH_KEY, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SKU_ID, "")
            Intent.FLAG_ACTIVITY_CLEAR_TASK
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }


}