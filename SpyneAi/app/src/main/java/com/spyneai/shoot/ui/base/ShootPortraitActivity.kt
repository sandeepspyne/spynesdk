package com.spyneai.shoot.ui.base


import CameraFragment
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
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
import com.spyneai.getImageCategory
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.setLocale
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.CategoryDetails
import com.spyneai.shoot.data.model.CreateProjectRes
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.ui.CreateProjectFragment
import com.spyneai.shoot.ui.DraftGridFragment
import com.spyneai.shoot.ui.SelectBackgroundFragment
import com.spyneai.shoot.ui.SubCategoryAndAngleFragment
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


class ShootPortraitActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    lateinit var cameraFragment: CameraFragment
    lateinit var gridEcomFragment: GridEcomFragment
    lateinit var overlayEcomFragment: OverlayEcomFragment
    lateinit var skuDetailFragment: SkuDetailFragment
    lateinit var projectDetailFragment: ProjectDetailFragment
    lateinit var selectBackgroundFragment: SelectBackgroundFragment
    lateinit var shootViewModel: ShootViewModel
    val location_data = JSONObject()
    val TAG = "ShootPortraitActivity"
    private var googleApiClient: GoogleApiClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_shoot)

        googleApiClient =
            GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build()

        setLocale()

        shootViewModel = ViewModelProvider(this, ViewModelFactory()).get(ShootViewModel::class.java)

        shootViewModel.skuNumber.value = 1
        try {
            shootViewModel.skuNumber.value = intent.getIntExtra("skuNumber", 1)
        } catch (e: Exception) {
        }

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
        gridEcomFragment = GridEcomFragment()
        skuDetailFragment = SkuDetailFragment()
        projectDetailFragment = ProjectDetailFragment()
        overlayEcomFragment = OverlayEcomFragment()
        selectBackgroundFragment = SelectBackgroundFragment()

        shootViewModel.processSku = false
        if (savedInstanceState == null) { // initial transaction should be wrapped like this
            val transaction = supportFragmentManager.beginTransaction()
                .add(R.id.flCamerFragment, cameraFragment)

            if (shootViewModel.fromDrafts) {
                when (categoryDetails.categoryId) {
                    AppConstants.FOOTWEAR_CATEGORY_ID -> {
                        transaction
                            .add(R.id.flCamerFragment, overlayEcomFragment)
                            .commitAllowingStateLoss()
                    }
                    else -> {
                        transaction
                            .add(R.id.flCamerFragment, DraftGridFragment())
                            .commitAllowingStateLoss()
                    }
                }
            } else {
                when (categoryDetails.categoryId) {
                    AppConstants.FOOTWEAR_CATEGORY_ID -> {
                        transaction.add(
                            R.id.flCamerFragment,
                            overlayEcomFragment
                        )

                        observeProjectCreated()
                    }
                    else -> transaction.add(R.id.flCamerFragment, gridEcomFragment)
                }
                transaction
                    .add(R.id.flCamerFragment, CreateProjectFragment())
                    .commitAllowingStateLoss()
            }
        }

        shootViewModel.categoryDetails.value?.imageType =
            getImageCategory(shootViewModel.categoryDetails.value!!.categoryId!!)


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
                    this.putExtra(
                        AppConstants.FROM_VIDEO,
                        intent.getBooleanExtra(AppConstants.FROM_VIDEO, false)
                    )
                    startActivity(this)
                }
            }
        })


    }

    private fun observeProjectCreated() {
        //add subcat selection fragment
        shootViewModel.getSubCategories.observe(
            this, {
                if (!shootViewModel.isSubcategoriesSelectionShown) {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.flCamerFragment, SubCategoryAndAngleFragment())
                        .commit()
                }
            }
        )
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
        shootViewModel.projectId.value = intent.getStringExtra(AppConstants.PROJECT_ID)!!

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
        sku.skuId = intent.getStringExtra(AppConstants.SKU_ID)
        sku.categoryName = shootViewModel.categoryDetails.value?.categoryName

        shootViewModel.sku.value = sku

        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) == "Footwear") {
            shootViewModel.exterirorAngles.value =
                intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0)
            shootViewModel.isSkuCreated.value = true
            //sub category selected
            shootViewModel.subCatName.value = intent.getStringExtra(AppConstants.SUB_CAT_NAME)

            shootViewModel.subCategory.value = NewSubCatResponse.Data(
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

            shootViewModel.isSubCategoryConfirmed.value = true

            // if (intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0) == intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)){
            shootViewModel.showDialog = false
            val list = shootViewModel.getImagesbySkuId(shootViewModel.sku.value?.skuId!!)

            shootViewModel.shootList.value = ArrayList()

            list.forEachIndexed { index, image ->
                val shootData = ShootData(
                    image.imagePath!!,
                    image.projectId!!,
                    image.skuId!!,
                    getImageCategory(intent.getStringExtra(AppConstants.CATEGORY_ID)!!),
                    Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(),
                    image.overlayId?.toInt()!!
                )

                shootData.imageClicked = true

                shootViewModel.shootList.value!!.add(
                    shootData
                )
            }


            // shootViewModel.stopShoot.value = true
            // }

        } else {
            shootViewModel.showDialog = false
            shootViewModel.isSubCategoryConfirmed.value = true

            shootViewModel.shootList.value = ArrayList()

            //set total clicked images
            val list = shootViewModel.getImagesbySkuId(shootViewModel.sku.value?.skuId!!)

            if (intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {

                list.forEachIndexed { index, image ->
                    val shootData = ShootData(
                        image.imagePath!!,
                        image.projectId!!,
                        image.skuId!!,
                        getImageCategory(intent.getStringExtra(AppConstants.CATEGORY_ID)!!),
                        Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(),
                        index.plus(1)
                    )

                    shootData.imageClicked = true
                    shootViewModel.shootList.value!!.add(
                        shootData
                    )
                }
            } else {
                val list = intent.getStringArrayListExtra(AppConstants.EXTERIOR_LIST)

                list?.forEachIndexed { index, image ->
                    val shootData = ShootData(
                        image,
                        intent.getStringExtra(AppConstants.PROJECT_ID)!!,
                        intent.getStringExtra(AppConstants.SKU_ID)!!,
                        getImageCategory(intent.getStringExtra(AppConstants.CATEGORY_ID)!!),
                        Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(),
                        index.plus(1)
                    )

                    shootData.imageClicked = true
                    shootViewModel.shootList.value!!.add(
                        shootData
                    )
                }


            }

        }
    }

    override fun onStart() {
        super.onStart()
        googleApiClient?.connect()
        shoot("onStart called(shhot activity)")
    }

    override fun onStop() {
        super.onStop()
        googleApiClient?.disconnect()
        shoot("onStop called(shoot activity)")
    }

    override fun onConnected(bundle: Bundle?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {

            try {
                val lastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                val lat: Double = lastLocation.latitude
                val lon: Double = lastLocation.longitude

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
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }


    override fun onBackPressed() {
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



