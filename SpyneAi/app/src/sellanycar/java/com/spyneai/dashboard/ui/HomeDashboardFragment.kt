package com.spyneai.dashboard.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import com.spyneai.R
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.draft.ui.DraftsActivity
import com.spyneai.fragment.TopUpFragment
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class HomeDashboardFragment :
    BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding>() {

    lateinit var btnlistener: CategoriesDashboardAdapter.BtnClickListener

    var categoryPosition: Int = 0
    lateinit var tokenId: String
    lateinit var email: String

    lateinit var catId: String
    lateinit var displayName: String
    lateinit var displayThumbnail: String
    lateinit var description: String
    lateinit var colorCode: String


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tokenId = Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString()
        email = Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID).toString()

        lisners()
    }



    private fun lisners(){
        binding.ivWallet.setOnClickListener {
            TopUpFragment().show(requireActivity().supportFragmentManager,"TopUpFragment")
        }

        binding.llDrafts.setOnClickListener {
            val intent = Intent(requireContext(), DraftsActivity::class.java)
            startActivity(intent)
        }

        binding.llCompleted.setOnClickListener {
            val intent = Intent(requireContext(), CompletedProjectsActivity::class.java)
            startActivity(intent)
        }

        binding.llOngoing.setOnClickListener {
            val intent = Intent(requireContext(), OngoingOrdersActivity::class.java)
            startActivity(intent)
        }
    }


    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = HomeDashboardFragmentBinding.inflate(inflater, container, false)

}