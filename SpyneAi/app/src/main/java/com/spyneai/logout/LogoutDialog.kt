package com.spyneai.logout

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.posthog.android.Properties
import com.spyneai.base.BaseDialogFragment
import com.spyneai.captureEvent
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.DialogLogoutBinding
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events

class LogoutDialog : BaseDialogFragment<DashboardViewModel, DialogLogoutBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llLogout.setOnClickListener {

            requireContext().captureEvent(Events.LOG_OUT, HashMap<String,Any?>())

            Utilities.savePrefrence(requireContext(), AppConstants.TOKEN_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.AUTH_KEY, "")
            Utilities.savePrefrence(requireContext(), AppConstants.PROJECT_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SKU_ID, "")
            Intent.FLAG_ACTIVITY_CLEAR_TASK
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)



            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogLogoutBinding.inflate(inflater, container, false)
}