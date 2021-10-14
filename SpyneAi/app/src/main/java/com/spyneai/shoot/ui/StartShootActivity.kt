package com.spyneai.shoot.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.spyneai.R
import com.spyneai.databinding.ActivityStartShootBinding
import com.spyneai.needs.AppConstants
import com.spyneai.setLocale
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.dialogs.RequiredPermissionDialog
import com.spyneai.threesixty.ui.ThreeSixtyIntroActivity

class StartShootActivity : AppCompatActivity(){

    lateinit var binding : ActivityStartShootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLocale()

        binding = ActivityStartShootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }




        binding.flShootNow.setOnClickListener{


            if (allPermissionsGranted()) {
                onPermissionGranted()

                val intent = Intent(this, ShootActivity::class.java)

                intent.putExtra(
                    AppConstants.CATEGORY_NAME,
                    "Automobiles")

                intent.putExtra(
                    AppConstants.CATEGORY_ID,
                    "cat_d8R14zUNE")

                startActivity(intent)



            } else {
                permissionRequest.launch(permissions.toTypedArray())
            }



        }

        binding.tvExplore.setOnClickListener {
            Intent(this,ThreeSixtyIntroActivity::class.java)
                .apply {
                    putExtra(AppConstants.CATEGORY_ID,"cat_d8R14zUNE")
                    putExtra(AppConstants.CATEGORY_NAME,"Automobiles")
                    startActivity(this)
                }
        }
    }

    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
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

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            val requiredPermissions = if (getString(R.string.app_name) == AppConstants.OLA_CABS){
                permissions
            }else{
                permissions.filter {
                    it.key != Manifest.permission.ACCESS_COARSE_LOCATION
                }
            }

            if (requiredPermissions.all {
                    it.value
                }) {
                onPermissionGranted()
            } else {

//                val intent = Intent(
//                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                    Uri.fromParts("package",packageName, null))
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)

                RequiredPermissionDialog().show(supportFragmentManager, "RequiredPermissionDialog")


//                Toast.makeText(this, R.string.message_no_permissions, Toast.LENGTH_SHORT).show()

            }

        }



    open fun onPermissionGranted() = Unit




}