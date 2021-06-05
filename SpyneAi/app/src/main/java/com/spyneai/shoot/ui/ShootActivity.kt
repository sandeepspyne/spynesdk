package com.spyneai.shoot.ui

import android.Manifest
import android.app.FragmentManager
import android.content.pm.PackageManager
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
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import com.spyneai.R


class ShootActivity : AppCompatActivity() {

    lateinit var cameraFragment: CameraFragment
    lateinit var overlaysFragment: OverlaysFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shoot)

        cameraFragment = CameraFragment()
        overlaysFragment = OverlaysFragment()

        if(savedInstanceState == null) { // initial transaction should be wrapped like this
            supportFragmentManager.beginTransaction()
                //.add(R.id.flCamerFragment, cameraFragment)
                .add(R.id.flCamerFragment, overlaysFragment)
                .commitAllowingStateLoss()
        }

        if (allPermissionsGranted()) {
            onPermissionGranted()
        } else {
            permissionRequest.launch(permissions.toTypedArray())
        }

    }

    /**
     * Check for the permissions
     */
    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    // The Folder location where all the files will be stored
    protected val outputDirectory: String by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${Environment.DIRECTORY_DCIM}/CameraXDemo/"
        } else {
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/CameraXDemo/"
        }
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

    fun View.snackbar(message: String, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        snackbar.show()
    }



}