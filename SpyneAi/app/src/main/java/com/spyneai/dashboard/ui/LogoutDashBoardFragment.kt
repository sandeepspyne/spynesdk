package com.spyneai.dashboard.ui

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
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities


class LogoutDashBoardFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.logout_dialog, container, false)

        logoutDialog()
    }

    private fun logoutDialog(){

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        var dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.logout_dialog, null)

        dialog.setContentView(dialogView)

        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        val llLogout: LinearLayout = dialog.findViewById(R.id.llLogout)
        var ivClose: ImageView = dialogView.findViewById(R.id.ivClose)
        var lottielogout: LottieAnimationView = dialogView.findViewById(R.id.lottielogout)


        ivClose.setOnClickListener(View.OnClickListener {

            dialog.dismiss()

        })

        dialog.show()


        llLogout.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            Utilities.savePrefrence(requireContext(), AppConstants.tokenId, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(requireContext(), AppConstants.SKU_ID, "")
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        })
    }

}