package com.spyneai.dashboard.ui.dashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import com.airbnb.lottie.LottieAnimationView
import com.spyneai.R
import com.spyneai.databinding.LogoutDialogBinding
import com.spyneai.databinding.WalletDashboardFragmentBinding
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities


class LogoutDashBoardFragment : Fragment() {


    private lateinit var binding : LogoutDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = LogoutDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.llLogout.setOnClickListener {
            Utilities.savePrefrence(requireContext(), AppConstants.tokenId, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SKU_ID, "")
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }


}