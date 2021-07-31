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
import androidx.lifecycle.Observer
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.fragment.TopUpFragment
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events


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




    private fun showFreeCreditDialog(message: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        var dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.free_credit_dialog, null)
        var tvMessage: TextView = dialogView.findViewById(R.id.tvSkuNameDialog)
        tvMessage.text = message

        dialog.setContentView(dialogView)

        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        val llOk: LinearLayout = dialog.findViewById(R.id.llOk)


        llOk.setOnClickListener(View.OnClickListener {

            dialog.dismiss()

        })
        dialog.show()

    }

    private fun lisners(){
        binding.ivWallet.setOnClickListener {
            TopUpFragment().show(requireActivity().supportFragmentManager,"TopUpFragment")
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