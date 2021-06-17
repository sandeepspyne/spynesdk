package com.spyneai.shoot.ui

import android.Manifest
import android.app.FragmentManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.balsikandar.kotlindslsamples.dialogfragment.DialogDSLBuilder.Companion.dialog
import com.google.android.material.snackbar.Snackbar
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.CategoryDetails
import com.spyneai.shoot.data.sqlite.DBHelper
import com.spyneai.shoot.data.sqlite.ShootContract
import kotlinx.android.synthetic.main.dialog_confirm_reshoot.view.*
import kotlinx.android.synthetic.main.dialog_exit_shoot.view.*
import kotlinx.android.synthetic.main.dialog_exit_shoot.view.btNo
import kotlinx.android.synthetic.main.dialog_focus_shoot.view.*
import kotlinx.android.synthetic.main.dialog_gif_hint.view.*
import kotlinx.android.synthetic.main.dialog_interior_shoot.view.*
import kotlinx.android.synthetic.main.dialog_interior_shoot.view.btShootInterior
import kotlinx.android.synthetic.main.dialog_shoot_hint.view.*
import java.io.File


class ShootActivity : AppCompatActivity() {

    lateinit var cameraFragment: CameraFragment
    lateinit var overlaysFragment: OverlaysFragment
    val TAG = "ShootActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shoot)

        val db = DBHelper(this).writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID, "sku id2")
            put(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES, 8)
            put(ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME, "automobiles")
        }

// Insert the new row, returning the primary key value of the new row
        val newRowId = db?.insert(ShootContract.ShootEntry.TABLE_NAME, null, values)

        Log.d(TAG, "onCreate: "+newRowId)

        // New value for one column
        val title = "sku id2"
        val valuess = ContentValues().apply {
            put(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES, 12)
        }

// Which row to update, based on the title
        val selections = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} LIKE ?"

        val selectionArgss = arrayOf("sku id2")

        val count = db.update(
            ShootContract.ShootEntry.TABLE_NAME,
            valuess,
            selections,
            selectionArgss)


        val dbR = DBHelper(this).readableDatabase

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        val projection = arrayOf(BaseColumns._ID,
            ShootContract.ShootEntry.COLUMN_NAME_SKU_ID,
            ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES)

// Filter results WHERE "title" = 'My Title'
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} = ?"
        val selectionArgs = arrayOf("sku id2")

// How you want the results sorted in the resulting Cursor
        val sortOrder = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} DESC"

        val cursor = dbR.query(
            ShootContract.ShootEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val skudId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID))
                //val cat = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME))
                val no = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES))

                Log.d(TAG, "onCreate: "+itemId+ " "+skudId+" "+no)
            }
        }

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


        shootViewModel.selectBackground.observe(this,{
            if (it) {
                // add select background fragment
            }
        })
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
}
