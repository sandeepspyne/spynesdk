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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.balsikandar.kotlindslsamples.dialogfragment.DialogDSLBuilder.Companion.dialog
import com.google.android.material.snackbar.Snackbar
import com.spyneai.R
import kotlinx.android.synthetic.main.dialog_confirm_reshoot.view.*
import kotlinx.android.synthetic.main.dialog_confirm_reshoot.view.btReshootImage
import kotlinx.android.synthetic.main.dialog_exit_shoot.view.*
import kotlinx.android.synthetic.main.dialog_exit_shoot.view.btNo
import kotlinx.android.synthetic.main.dialog_focus_shoot.view.*
import kotlinx.android.synthetic.main.dialog_gif_hint.view.*
import kotlinx.android.synthetic.main.dialog_interior_shoot.view.*
import kotlinx.android.synthetic.main.dialog_interior_shoot.view.btShootInterior
import kotlinx.android.synthetic.main.dialog_shoot_hint.view.*


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
                .add(R.id.flCamerFragment, overlaysFragment)
                .add(R.id.flCamerFragment, cameraFragment)
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

    fun showExitShootDialog(view: View) {
        dialog {
            layoutId = R.layout.dialog_exit_shoot
            setCustomView = {it: View, dialog: DialogFragment ->


                it.btNo.setOnClickListener {
                    Toast.makeText(this@ShootActivity, "No", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

                it.btYes.setOnClickListener {
                    Toast.makeText(this@ShootActivity, "Yes", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

            }
        }
    }

    fun showConfirmReshootDialog(view: View) {
        dialog {
            layoutId = R.layout.dialog_confirm_reshoot
            setCustomView = {it: View, dialog: DialogFragment ->


                it.btReshootImage.setOnClickListener {
                    Toast.makeText(this@ShootActivity, "Reshoot Image", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

                it.btConfirmImage.setOnClickListener {
                    Toast.makeText(this@ShootActivity, "Confirm Image", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

            }
        }
    }

    fun showGifHintDialog(view: View) {
        dialog {
            layoutId = R.layout.dialog_shoot_hint
            setCustomView = {it: View, dialog: DialogFragment ->


                it.btContinue.setOnClickListener {
                    Toast.makeText(this@ShootActivity, "Continue", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }


            }
        }
    }

    fun showInteriorShootDialog(view: View) {
        dialog {
            layoutId = R.layout.dialog_interior_shoot
            setCustomView = {it: View, dialog: DialogFragment ->


                it.btSkip.setOnClickListener {
                    Toast.makeText(this@ShootActivity, "Skip", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

                it.btShootInterior.setOnClickListener {
                    Toast.makeText(this@ShootActivity, "Shoot Interior", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }


            }
        }
    }

    fun showFocusShootDialog(view: View) {
        dialog {
            layoutId = R.layout.dialog_focus_shoot
            setCustomView = {it: View, dialog: DialogFragment ->


                it.tvSkip.setOnClickListener {
                    Toast.makeText(this@ShootActivity, "Skip", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }

                it.tvShoot.setOnClickListener {
                    Toast.makeText(this@ShootActivity, "Shoot Interior", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }


            }
        }
    }



}