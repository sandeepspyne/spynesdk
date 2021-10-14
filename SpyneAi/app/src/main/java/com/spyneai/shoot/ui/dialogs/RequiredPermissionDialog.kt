package com.spyneai.shoot.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogExitBinding
import com.spyneai.databinding.RequiredPermissionDialogBinding
import com.spyneai.gotoHome
import com.spyneai.shoot.data.ShootViewModel


class RequiredPermissionDialog : BaseDialogFragment<ShootViewModel,RequiredPermissionDialogBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btGivePermission.setOnClickListener {

            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireContext().packageName, null))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = RequiredPermissionDialogBinding.inflate(inflater, container, false)
}

