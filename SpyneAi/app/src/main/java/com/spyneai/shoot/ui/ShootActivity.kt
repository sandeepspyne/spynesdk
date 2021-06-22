package com.spyneai.shoot.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.CategoryDetails
import com.spyneai.shoot.ui.dialogs.ShootExitDialog
import java.io.File


class ShootActivity : AppCompatActivity() {

    lateinit var cameraFragment: CameraFragment
    lateinit var overlaysFragment: OverlaysFragment
    val TAG = "ShootActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_shoot)

        val shootViewModel = ViewModelProvider(this, ViewModelFactory()).get(ShootViewModel::class.java)

        val categoryDetails = CategoryDetails()

        categoryDetails.apply {
           categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)
            gifList =  intent.getStringExtra(AppConstants.GIF_LIST)
        }

        shootViewModel.categoryDetails.value = categoryDetails

        cameraFragment = CameraFragment()
        overlaysFragment = OverlaysFragment()

        if(savedInstanceState == null) { // initial transaction should be wrapped like this
            supportFragmentManager.beginTransaction()
                .add(R.id.flCamerFragment, cameraFragment)
                .add(R.id.flCamerFragment, overlaysFragment)
                .commitAllowingStateLoss()
        }

        if (allPermissionsGranted()) {
            onPermissionGranted()
        } else {
            permissionRequest.launch(permissions.toTypedArray())
        }


        shootViewModel.selectBackground.observe(this, {
            if (it) {
                // start process activity
                val intent = Intent(this, ProcessActivity::class.java)

                intent.apply {
                    this.putExtra("sku_id", shootViewModel.sku.value?.skuId)
                    this.putExtra("exterior_angles", shootViewModel.exterirorAngles.value)
                    startActivity(this)
                }
            }
        })
    }

    /**
     * Check for the permissions
     */
    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            onPermissionGranted()
        } else {
            Toast.makeText(this, R.string.message_no_permissions, Toast.LENGTH_SHORT).show()
        }
    }

    open fun onPermissionGranted() = Unit


    companion object {
        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

    override fun onBackPressed() {
        ShootExitDialog().show(supportFragmentManager,"ShootExitDialog")
    }
}
