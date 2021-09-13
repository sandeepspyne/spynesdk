package com.spyneai.dashboard.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.BuildConfig
import com.spyneai.R
import com.spyneai.activity.CategoriesActivity
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityDashboardMainBinding
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.orders.ui.MyOrdersFragment
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.service.manual.StoreImageFiles
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.response.UploadFolderRes
import com.spyneai.shoot.ui.StartShootActivity
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.dialogs.ResolutionNotSupportedFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainDashboardActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDashboardMainBinding
    private var viewModel : DashboardViewModel? = null
    private var TAG = "MainDashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent.getBooleanExtra("show_ongoing",false)){
            val intent = Intent(this, MyOrdersActivity::class.java)
            startActivity(intent)
        }

        binding.bottomNavigation.background = null
        viewModel = ViewModelProvider(this, ViewModelFactory()).get(DashboardViewModel::class.java)

        val firstFragment= HomeDashboardFragment()
        val SecondFragment=WalletDashboardFragment()
        val myOrdersFragment= MyOrdersFragment()
        val thirdFragment=LogoutDashBoardFragment()

        //save category id and name
        Utilities.savePrefrence(this,AppConstants.CATEGORY_ID,AppConstants.CARS_CATEGORY_ID)
        Utilities.savePrefrence(this,AppConstants.CATEGORY_NAME,"Automobiles")


        when(getString(R.string.app_name)) {
            AppConstants.SPYNE_AI -> {
                binding.fab.setOnClickListener {
                    val intent = Intent(this, CategoriesActivity::class.java)
                    startActivity(intent)
                }
            }else -> {
                binding.fab.visibility = View.GONE
            }
        }


        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.homeDashboardFragment->setCurrentFragment(firstFragment)

                R.id.shootActivity-> {
                    when(getString(R.string.app_name)) {
                        "Ola Cabs",
                        AppConstants.CARS24,
                        AppConstants.CARS24_INDIA,
                        "Trusted cars",
                        "Travo Photos",
                        "Yalla Motors",
                        "Spyne Hiring",
                        AppConstants.AUTO_MOSER-> {
                            var intent = Intent(this, StartShootActivity::class.java)
                            intent.putExtra(AppConstants.CATEGORY_ID,AppConstants.CARS_CATEGORY_ID)
                            intent.putExtra(AppConstants.CATEGORY_NAME,"Automobiles")
                            startActivity(intent)

                        }

                        AppConstants.KARVI -> {
                            val cm = getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager

                            if (cm.cameraIdList != null && cm.cameraIdList.size > 1) {
                                val characteristics: CameraCharacteristics =
                                    cm.getCameraCharacteristics("1")

                                val configs = characteristics.get(
                                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                                )

                                val s = configs?.getOutputSizes(ImageFormat.JPEG)

                                var resolutionSupported = true

                                s?.forEach { it ->
                                    if (!resolutionSupported && it != null) {
                                        if (it.width == 1024 && it.height == 768)
                                            resolutionSupported = true
                                    }
                                }

                                if (resolutionSupported) {
                                    var intent = Intent(this, ShootActivity::class.java)
                                    intent.putExtra(AppConstants.CATEGORY_ID,AppConstants.CARS_CATEGORY_ID)
                                    intent.putExtra(AppConstants.CATEGORY_NAME,"Automobiles")
                                    startActivity(intent)
                                }else {
                                    //resolution not supported
                                    ResolutionNotSupportedFragment().show(supportFragmentManager,"ResolutionNotSupportedFragment")
                                }
                            }else {
                                //resolution not supported
                                ResolutionNotSupportedFragment().show(supportFragmentManager,"ResolutionNotSupportedFragment")
                            }
                        }

                        "Flipkart", "Udaan", "Lal10", "Amazon", "Swiggy" -> {
                            val intent = Intent(this@MainDashboardActivity, CategoriesActivity::class.java)
                                startActivity(intent)
                            }
                        else ->{
                            var intent = Intent(this, ShootActivity::class.java)
                            intent.putExtra(AppConstants.CATEGORY_ID,AppConstants.CARS_CATEGORY_ID)
                            intent.putExtra(AppConstants.CATEGORY_NAME,"Automobiles")
                            startActivity(intent)
                        }
                    }
                }

                R.id.completedOrdersFragment-> {
                   if (getString(R.string.app_name) == AppConstants.SPYNE_AI)
                       setCurrentFragment(myOrdersFragment)
                   else{
                       val intent = Intent(this, MyOrdersActivity::class.java)
                       startActivity(intent)
                   }
                }
                //R.id.wallet->setCurrentFragment(SecondFragment)
                R.id.logoutDashBoardFragment->setCurrentFragment(thirdFragment)
            }
            true
        }

        if (intent.getBooleanExtra(AppConstants.IS_NEW_USER,false)){
            viewModel!!.isNewUser.value = intent.getBooleanExtra(AppConstants.IS_NEW_USER,false)
            viewModel!!.creditsMessage.value = intent.getStringExtra(AppConstants.CREDITS_MESSAGE)
        }

        checkAppVersion()
        observeAppVersion()
    }

    private fun checkAppVersion() {
        if (BuildConfig.VERSION_NAME.contains("debug")){
            if (allPermissionsGranted()) {
                onPermissionGranted()
            } else {
                permissionRequest.launch(permissions.toTypedArray())
            }
        }else{
            Utilities.showProgressDialog(this)

            viewModel?.getVersionStatus(
                Utilities.getPreference(this,AppConstants.AUTH_KEY).toString(),
                BuildConfig.VERSION_NAME
            )
        }
    }

    private fun observeAppVersion() {
        viewModel?.versionResponse?.observe(this,{
            when(it){
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    if (allPermissionsGranted()) {
                        onPermissionGranted()
                    } else {
                        permissionRequest.launch(permissions.toTypedArray())
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    if (it.errorCode == 400){
                        //show update app dialog
                        OutdatedVersionDialog().show(supportFragmentManager,"OutdatedVersionDialog")
                    }else{
                        handleApiError(it) { checkAppVersion() }
                    }
                }
            }
        })
    }

    private val permissions = mutableListOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            onPermissionGranted()
            capture(Events.PERMISSIONS_GRANTED)
        } else {
            capture(Events.PERMISSIONS_DENIED)
            Snackbar.make(binding.root, "App cannot work without permission", Snackbar.LENGTH_INDEFINITE)
                .setAction("Allow") {
                   requestPermi()
                }
                .setActionTextColor(ContextCompat.getColor(this,R.color.primary))
                .show()
        }
    }

    private fun capture(eventName : String) {
        val properties = Properties()
        properties.apply {
            this["email"] = Utilities.getPreference(this@MainDashboardActivity,AppConstants.EMAIL_ID).toString()
        }

        captureEvent(
            eventName,
            properties)
    }

    private fun requestPermi() {
        permissionRequest.launch(permissions.toTypedArray())
    }

    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    open fun onPermissionGranted(){
        Log.d(TAG, "onPermissionGranted: "+Utilities.getPreference(this,AppConstants.CANCEL_ALL_WROKERS))
        cancelAllWorkers()

        startUploadService()
        checkFolderUpload()
    }

    private fun startUploadService() {
        val shootLocalRepository = ShootLocalRepository()
        if (shootLocalRepository.getOldestImage().itemId != null
            || shootLocalRepository.getOldestSkippedImage().itemId != null){

            var action = Actions.START
            if (getServiceState(this) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
                return

            val serviceIntent = Intent(this, ImageUploadingService::class.java)
            serviceIntent.action = action.name

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                ContextCompat.startForegroundService(this, serviceIntent)
                return
            } else {
                log("Starting the service in < 26 Mode")
                startService(serviceIntent)
            }

            val properties = Properties()
                .apply {
                    put("service_state","Started")
                    put("email",Utilities.getPreference(this@MainDashboardActivity,AppConstants.EMAIL_ID).toString())
                    put("medium","Main Actity")
                }

            captureEvent(Events.SERVICE_STARTED,properties)
        }
    }

    private fun cancelAllWorkers(){
        //cancel all workers
        WorkManager.getInstance(this).cancelAllWork()

        WorkManager.getInstance(this).cancelAllWorkByTag("StoreImageFiles  Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("Manual Long Running Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("Manual Skipped Images Long Running Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("Long Running Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("Skipped Images Long Running Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("Periodic Processing Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("InternetWorker")

        Utilities.savePrefrence(this,AppConstants.CANCEL_ALL_WROKERS,"Cancelled")
    }

    private fun checkFolderUpload() {
        Utilities.showProgressDialog(this)

        val request = RetrofitClients.buildService(ClipperApi::class.java)
        val authKey = Utilities.getPreference(this, AppConstants.AUTH_KEY).toString()

        val call = request.uploadFolder(authKey)

        call.enqueue(object : Callback<UploadFolderRes>{
            override fun onResponse(
                call: Call<UploadFolderRes>,
                response: Response<UploadFolderRes>
            ) {

                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.body()?.status == 200){
                        if (response.body()?.data?.isFolderUpload == 1){
                            if (Utilities.getPreference(this@MainDashboardActivity,AppConstants.START_FILES_WORKER) == ""){

                                GlobalScope.launch {
                                    StoreImageFiles(this@MainDashboardActivity,
                                        ShootRepository(),
                                        FilesRepository()
                                    ).startWork()
                                }

                                capture(Events.FILE_READ_WORKED_INTIATED)
                            }else {
                                capture(Events.FILE_WORKER_ALREADY_INTIATED)
                            }
                        }else {
                            capture(Events.FILE_FOLDER_UPLOAD_FALSE)
                        }
                    }else {
                        val properties = Properties()
                        properties.apply {
                            this["email"] = Utilities.getPreference(this@MainDashboardActivity,AppConstants.EMAIL_ID).toString()
                            this["error"] = response.body()?.message
                        }

                        captureEvent(
                            Events.CHECK_FOLDER_API_FAILED,
                            properties)
                    }
                }else {
                    val properties = Properties()
                    properties.apply {
                        this["email"] = Utilities.getPreference(this@MainDashboardActivity,AppConstants.EMAIL_ID).toString()
                        this["error"] = response.errorBody().toString()
                    }

                    captureEvent(
                        Events.CHECK_FOLDER_API_FAILED,
                        properties)
                }
            }

            override fun onFailure(call: Call<UploadFolderRes>, t: Throwable) {
                val properties = Properties()
                properties.apply {
                    this["email"] = Utilities.getPreference(this@MainDashboardActivity,AppConstants.EMAIL_ID).toString()
                    this["error"] = t.localizedMessage
                }

                captureEvent(
                    Events.CHECK_FOLDER_API_FAILED,
                    properties)

                Utilities.hideProgressDialog()
                folderCheckError(t.localizedMessage)
            }
        })
    }

    private fun folderCheckError(error : String) {
        Snackbar.make(binding.root, error, Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry") {
                checkFolderUpload()
            }
            .setActionTextColor(ContextCompat.getColor(this,R.color.primary))
            .show()
    }

    override fun onResume() {
        super.onResume()

        FilesRepository().getAllImages()
       binding.bottomNavigation.selectedItemId = R.id.homeDashboardFragment
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(binding.flContainer.id,fragment)
            commit()
        }

    override fun onBackPressed() {
       if (binding.bottomNavigation.selectedItemId != R.id.homeDashboardFragment)
           binding.bottomNavigation.selectedItemId = R.id.homeDashboardFragment
        else
         super.onBackPressed()
    }
}