package com.spyneai

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.FragmentShootSiteDialogBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootViewModel

class Shoot_Site_Dialog : BaseDialogFragment<DashboardViewModel, FragmentShootSiteDialogBinding>() {






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        binding.btCamera.setOnClickListener {

            viewModel.isStartAttendance.value = true


            val s= ""

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
    ) = FragmentShootSiteDialogBinding.inflate(inflater, container, false)
}