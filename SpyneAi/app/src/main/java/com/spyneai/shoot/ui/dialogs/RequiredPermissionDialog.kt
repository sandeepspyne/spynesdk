package com.spyneai.shoot.ui.dialogs

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogExitBinding
import com.spyneai.databinding.RequiredPermissionDialogBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootViewModel


class RequiredPermissionDialog : BaseDialogFragment<ShootViewModel,RequiredPermissionDialogBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        binding.btGivePermission.setOnClickListener {

            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireContext().packageName, null))
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
           // requireActivity().startActivityForResult(intent,1000)
            resultLauncher.launch(intent)
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (allPermissionsGranted())
            dismiss()
    }

    private val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }

    protected fun allPermissionsGranted() : Boolean {
        val requiredPermissions = if (getString(R.string.app_name) == AppConstants.OLA_CABS){
            permissions
        }else{
            permissions.filter {
                it != Manifest.permission.ACCESS_COARSE_LOCATION
            }
        }

        val s = ""

        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
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

