package com.spyneai.shoot.ui.base


import CameraFragment
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.setLocale
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.CategoryDetails
import com.spyneai.shoot.data.model.CreateProjectRes
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.ui.OverlaysFragment
import com.spyneai.shoot.ui.SelectBackgroundFragment
import com.spyneai.shoot.ui.dialogs.RequiredPermissionDialog
import com.spyneai.shoot.ui.dialogs.ShootExitDialog
import com.spyneai.shoot.ui.ecomwithgrid.GridEcomFragment
import com.spyneai.shoot.ui.ecomwithgrid.ProjectDetailFragment
import com.spyneai.shoot.ui.ecomwithgrid.SkuDetailFragment
import com.spyneai.shoot.ui.ecomwithoverlays.OverlayEcomFragment
import com.spyneai.shoot.utils.shoot
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ShootActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    lateinit var cameraFragment: CameraFragment
    lateinit var overlaysFragment: OverlaysFragment
    lateinit var gridEcomFragment: GridEcomFragment
    lateinit var overlayEcomFragment: OverlayEcomFragment
    lateinit var skuDetailFragment: SkuDetailFragment
    lateinit var projectDetailFragment: ProjectDetailFragment
    lateinit var selectBackgroundFragment: SelectBackgroundFragment
    lateinit var shootViewModel: ShootViewModel
    val location_data = JSONObject()
    val TAG = "ShootActivity"
    private var googleApiClient: GoogleApiClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shoot("onCreate called(shoot activity)")

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_shoot)


        googleApiClient =
            GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build()

        setLocale()


        shootViewModel = ViewModelProvider(this, ViewModelFactory()).get(ShootViewModel::class.java)

        if (intent.getBooleanExtra(AppConstants.FROM_DRAFTS, false))
            setUpDraftsData()

        if (intent.getBooleanExtra(AppConstants.FROM_VIDEO, false))
            setUpVideoShoot()

        val categoryDetails = CategoryDetails()

        categoryDetails.apply {
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)
            gifList = intent.getStringExtra(AppConstants.GIF_LIST)
        }

        shootViewModel.categoryDetails.value = categoryDetails

        cameraFragment = CameraFragment()
        overlaysFragment = OverlaysFragment()
        gridEcomFragment = GridEcomFragment()
        skuDetailFragment = SkuDetailFragment()
        projectDetailFragment = ProjectDetailFragment()
        overlayEcomFragment = OverlayEcomFragment()
        selectBackgroundFragment = SelectBackgroundFragment()

        when (shootViewModel.categoryDetails.value?.categoryName) {
            "Automobiles" -> {
                shootViewModel.processSku = true
                if (savedInstanceState == null) { // initial transaction should be wrapped like this
                    supportFragmentManager.beginTransaction()
                        .add(R.id.flCamerFragment, cameraFragment)
                        .add(R.id.flCamerFragment, overlaysFragment)
                        .commitAllowingStateLoss()
                }
            }
            "Bikes" -> {
                shootViewModel.processSku = false
                if (savedInstanceState == null) { // initial transaction should be wrapped like this
                    supportFragmentManager.beginTransaction()
                        .add(R.id.flCamerFragment, cameraFragment)
                        .add(R.id.flCamerFragment, overlaysFragment)
                        .commitAllowingStateLoss()
                }
            }

            "E-Commerce" -> {
                shootViewModel.processSku = false
                if (savedInstanceState == null) { // initial transaction should be wrapped like this
                    supportFragmentManager.beginTransaction()
                        .add(R.id.flCamerFragment, cameraFragment)
                        .add(R.id.flCamerFragment, gridEcomFragment)
                        .commitAllowingStateLoss()
                }
                try {
                    val intent = intent
                    shootViewModel.projectId.value = intent.getStringExtra("project_id")
                    val sku = Sku()
                    sku?.projectId = shootViewModel.projectId.value
                    shootViewModel.categoryDetails.value?.imageType = "Ecom"
                    shootViewModel.sku.value = sku
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            "Photo Box" -> {
                shootViewModel.processSku = false
                if (savedInstanceState == null) { // initial transaction should be wrapped like this
                    supportFragmentManager.beginTransaction()
                        .add(R.id.flCamerFragment, cameraFragment)
                        .add(R.id.flCamerFragment, gridEcomFragment)
                        .commitAllowingStateLoss()
                }
                try {
                    val intent = intent
                    shootViewModel.projectId.value = intent.getStringExtra("project_id")
                    val sku = Sku()
                    sku?.projectId = shootViewModel.projectId.value
                    shootViewModel.categoryDetails.value?.imageType = "Ecom"
                    shootViewModel.sku.value = sku
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            "Food & Beverages" -> {
                shootViewModel.processSku = false
                if (savedInstanceState == null) { // initial transaction should be wrapped like this
                    supportFragmentManager.beginTransaction()
                        .add(R.id.flCamerFragment, cameraFragment)
                        .add(R.id.flCamerFragment, gridEcomFragment)
                        .commitAllowingStateLoss()
                }
                try {
                    val intent = intent
                    shootViewModel.projectId.value = intent.getStringExtra("project_id")
                    val sku = Sku()
                    sku?.projectId = shootViewModel.projectId.value
                    shootViewModel.categoryDetails.value?.imageType = "Food"
                    shootViewModel.sku.value = sku
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            "Footwear" -> {
                shootViewModel.processSku = false
                if (savedInstanceState == null) { // initial transaction should be wrapped like this
                    supportFragmentManager.beginTransaction()
                        .add(R.id.flCamerFragment, cameraFragment)
                        .add(R.id.flCamerFragment, overlayEcomFragment)
                        .commitAllowingStateLoss()
                }
                try {
                    val intent = intent
                    shootViewModel.projectId.value = intent.getStringExtra("project_id")
                    val sku = Sku()
                    sku?.projectId = shootViewModel.projectId.value
                    shootViewModel.categoryDetails.value?.imageType = "Footwear"
                    shootViewModel.sku.value = sku
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

//        if (allPermissionsGranted()) {
//            onPermissionGranted()
//        } else {
//            permissionRequest.launch(permissions.toTypedArray())
//        }

        shootViewModel.stopShoot.observe(this, {
            if (it) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                supportFragmentManager.beginTransaction()
                    .add(R.id.flCamerFragment, skuDetailFragment)
                    .commit()
            }
        })

        shootViewModel.showProjectDetail.observe(this, {
            if (it) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                supportFragmentManager.beginTransaction().remove(skuDetailFragment).commit()
                supportFragmentManager.beginTransaction().remove(cameraFragment).commit()
                supportFragmentManager.beginTransaction().remove(gridEcomFragment).commit()
                supportFragmentManager.beginTransaction()
                    .add(R.id.flCamerFragment, projectDetailFragment)
                    .commit()
            }
        })

        shootViewModel.showFoodBackground.observe(this, {
            if (it) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                supportFragmentManager.beginTransaction().remove(skuDetailFragment).commit()
                supportFragmentManager.beginTransaction().remove(projectDetailFragment).commit()
                supportFragmentManager.beginTransaction().remove(cameraFragment).commit()
                supportFragmentManager.beginTransaction().remove(gridEcomFragment).commit()
                supportFragmentManager.beginTransaction()
                    .add(R.id.flCamerFragment, selectBackgroundFragment)
                    .commit()
            }
        })

        shootViewModel.addMoreAngle.observe(this, {
            if (it)
                supportFragmentManager.beginTransaction().remove(skuDetailFragment).commit()
        })

        shootViewModel.selectBackground.observe(this, {
            if (it) {
                // start process activity
                val processIntent = Intent(this, ProcessActivity::class.java)
                processIntent.apply {
                    this.putExtra(AppConstants.CATEGORY_NAME, categoryDetails.categoryName)
                    this.putExtra("sku_id", shootViewModel.sku.value?.skuId)
                    this.putExtra("project_id", shootViewModel.sku.value?.projectId)
                    this.putExtra("exterior_angles", shootViewModel.exterirorAngles.value)
                    this.putExtra("process_sku", shootViewModel.processSku)
                    this.putExtra("interior_misc_count", getInteriorMiscCount())
                    this.putStringArrayListExtra("exterior_images_list", getExteriorImagesList())
                    this.putExtra(
                        AppConstants.FROM_VIDEO,
                        intent.getBooleanExtra(AppConstants.FROM_VIDEO, false)
                    )
                    startActivity(this)
                }
            }
        })
    }


    private fun setUpVideoShoot() {


        shootViewModel.fromVideo = true
        shootViewModel.showVin.value = true
        shootViewModel.isProjectCreated.value = true
        shootViewModel.projectId.value = intent.getStringExtra(AppConstants.PROJECT_ID)

        shootViewModel._createProjectRes.value = Resource.Success(
            CreateProjectRes(
                "",
                intent.getStringExtra(AppConstants.PROJECT_ID)!!,
                200
            )
        )
    }

    private fun setUpDraftsData() {
        shootViewModel.fromDrafts = true
        shootViewModel.showVin.value = true
        shootViewModel.isProjectCreated.value = true
        shootViewModel.projectId.value = intent.getStringExtra(AppConstants.PROJECT_ID)

        shootViewModel._createProjectRes.value = Resource.Success(
            CreateProjectRes(
                "",
                intent.getStringExtra(AppConstants.PROJECT_ID)!!,
                200
            )
        )

        //set sku data
        val sku = Sku()
        sku.projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
        sku.skuName = intent.getStringExtra(AppConstants.SKU_NAME)
        sku.categoryName = shootViewModel.categoryDetails.value?.categoryName

        shootViewModel.sku.value = sku

        if (intent.getBooleanExtra(AppConstants.SKU_CREATED, false)) {
            shootViewModel.exterirorAngles.value =
                intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0)

            shootViewModel.getSubCategories(
                Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(),
                intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
            )

            shootViewModel.sku.value!!.skuId = intent.getStringExtra(AppConstants.SKU_ID)

            //fetch overlays
            shootViewModel.isSubCategoryConfirmed.value = true
            shootViewModel.subCategory.value = getSubcategoryResponse()

            shootViewModel.getOverlays(
                Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(),
                intent.getStringExtra(AppConstants.CATEGORY_ID)!!,
                intent.getStringExtra(AppConstants.SUB_CAT_ID)!!,
                intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0).toString(),
            )
        }

    }

    private fun getSubcategoryResponse(): NewSubCatResponse.Data? {
        return NewSubCatResponse.Data(
            1,
            "",
            "",
            "",
            1,
            1,
            intent.getStringExtra(AppConstants.CATEGORY_ID)!!,
            intent.getStringExtra(AppConstants.SUB_CAT_ID)!!,
            intent.getStringExtra(AppConstants.SUB_CAT_NAME)!!,
            ""
        )
    }

    override fun onStart() {
        super.onStart()
        googleApiClient?.isConnected()
        shoot("onStart called(shhot activity)")
    }

    override fun onResume() {
        super.onResume()
        shoot("onResume called(shoot activity)")
    }

    override fun onPause() {
        super.onPause()
        shoot("onPause called(shoot activity)")
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient?.isConnected() == true) {
            googleApiClient?.disconnect()
        }
        shoot("onStop called(shoot activity)")
    }

    override fun onRestart() {
        super.onRestart()
        shoot("onRestart called(shoot activity)")
    }

    override fun onDestroy() {
        super.onDestroy()
        shoot("onDistroy called(shoot activity)")
    }

    private fun getInteriorMiscCount(): Int {
        var total = 0

        val list = shootViewModel.shootList.value

        val interiorList = list?.filter {
            it.image_category == "Interior"
        }

        val miscList = list?.filter {
            it.image_category == "Focus Shoot"
        }

        if (interiorList != null)
            total = interiorList.size


        if (miscList != null)
            total += miscList.size

        if (shootViewModel.fromDrafts) {
            total += intent.getIntExtra(AppConstants.INTERIOR_SIZE, 0)
            total += intent.getIntExtra(AppConstants.MISC_SIZE, 0)
        }

        if (getString(R.string.app_name) == AppConstants.OLA_CABS
            && shootViewModel.threeSixtyInteriorSelected
        ) {
            total += 1
        }

        if (intent.getBooleanExtra(AppConstants.FROM_VIDEO, false)) {
            total += intent.getIntExtra(AppConstants.TOTAL_FRAME, 0)
        }

        Log.d(TAG, "getInteriorMiscCount: " + total)

        return total
    }

    private fun getExteriorImagesList(): ArrayList<String> {
        val exteriorList = shootViewModel.shootList.value?.filter {
            it.image_category == "Exterior"
        }

        val s = exteriorList?.map {
            it.capturedImage
        }

        return if (s == null) ArrayList() else s as ArrayList<String>
    }

    /**
     * Check for the permissions
     */
//    protected fun allPermissionsGranted() = permissions.all {
//        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private val permissions = mutableListOf(
//        Manifest.permission.CAMERA,
//        Manifest.permission.READ_EXTERNAL_STORAGE,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.ACCESS_COARSE_LOCATION,
//    ).apply {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
//        }
//    }

//    private val permissionRequest =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//
//            val requiredPermissions = if (getString(R.string.app_name) == AppConstants.OLA_CABS){
//                permissions
//            }else{
//                permissions.filter {
//                    it.key != Manifest.permission.ACCESS_COARSE_LOCATION
//                }
//            }
//
//            if (requiredPermissions.all {
//                    it.value
//                }) {
//                onPermissionGranted()
//            } else {
//
////                val intent = Intent(
////                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
////                    Uri.fromParts("package",packageName, null))
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////                startActivity(intent)
//
//                RequiredPermissionDialog().show(supportFragmentManager, "RequiredPermissionDialog")
//
//
//                Toast.makeText(this, R.string.message_no_permissions, Toast.LENGTH_SHORT).show()
//
//            }
//
//        }


    override fun onConnected(bundle: Bundle?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val lastLocation: Location =
                LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            val lat: Double = lastLocation.getLatitude()
            val lon: Double = lastLocation.getLongitude()

            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(lat, lon, 1)
            val postalCode = addresses[0].postalCode
            val cityName = addresses[0].locality
            val countryName = addresses[0].countryName

            location_data.put("city", cityName)
            location_data.put("country", countryName)
            location_data.put("latitude", lat)
            location_data.put("longitude", lon)
            location_data.put("postalCode", postalCode)

            Log.d(TAG, "onConnected: $location_data")

            shootViewModel.location_data.value = location_data


        }

    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }


//    open fun onPermissionGranted() = Unit


    companion object {
        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }


    override fun onBackPressed() {
        if (intent.getBooleanExtra(AppConstants.FROM_DRAFTS, false))
            ShootExitDialog().show(supportFragmentManager, "ShootExitDialog")
        else
            ShootExitDialog().show(supportFragmentManager, "ShootExitDialog")
    }

    // 1. onKeyDown is a boolean function, which returns the state of the KeyEvent.
    // 4. This code can be used to check if the device responds to any Key.
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event?.repeatCount == 0) {
                    if (shootViewModel.onVolumeKeyPressed.value == null)
                        shootViewModel.onVolumeKeyPressed.value = true
                    else
                        shootViewModel.onVolumeKeyPressed.value =
                            !shootViewModel.onVolumeKeyPressed.value!!
                }
            }

            KeyEvent.KEYCODE_BACK -> {
                onBackPressed()
            }
        }
        return true
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }
}




