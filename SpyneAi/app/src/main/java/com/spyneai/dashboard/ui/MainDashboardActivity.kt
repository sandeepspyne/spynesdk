package com.spyneai.dashboard.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.spyneai.*
import com.spyneai.R
import com.spyneai.activity.CategoriesActivity
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityDashboardMainBinding
import com.spyneai.debug.SendSkusData
import com.spyneai.fragment.PreferenceFragment
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.orders.ui.fragment.MyOrdersFragment
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.service.manual.ManualUploadService
import com.spyneai.service.manual.StoreImageFiles
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.response.UploadFolderRes
import com.spyneai.shoot.ui.StartShootActivity
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.dialogs.NoMagnaotoMeterDialog
import com.spyneai.shoot.ui.dialogs.RequiredPermissionDialog
import com.spyneai.threesixty.data.VideoLocalRepository
import com.spyneai.threesixty.data.VideoUploadService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MainDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardMainBinding
    private var viewModel: DashboardViewModel? = null
    private var TAG = "MainDashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setLocale()

        if (intent.getBooleanExtra("show_ongoing", false)) {
            val intent = Intent(this, MyOrdersActivity::class.java)
            intent.putExtra("TAB_ID", 1)
            startActivity(intent)
        }

        binding.bottomNavigation.background = null
        viewModel = ViewModelProvider(this, ViewModelFactory()).get(DashboardViewModel::class.java)

        val firstFragment = HomeDashboardFragment()
        val SecondFragment = WalletDashboardFragment()
        val myOrdersFragment = MyOrdersFragment()
        val thirdFragment = PreferenceFragment()

        //save category id and name
        Utilities.savePrefrence(this, AppConstants.CATEGORY_ID, AppConstants.CARS_CATEGORY_ID)
        Utilities.savePrefrence(this, AppConstants.CATEGORY_NAME, "Automobiles")


        when (getString(R.string.app_name)) {
            AppConstants.SPYNE_AI -> {
                binding.fab.setOnClickListener {
                    val intent = Intent(this, CategoriesActivity::class.java)
                    startActivity(intent)
                }
            }
            else -> {
                binding.fab.visibility = View.GONE
            }
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener {

            Log.d(TAG, "onCreate: " + getString(R.string.app_name))

            when (it.itemId) {
                R.id.homeDashboardFragment -> setCurrentFragment(firstFragment)

                R.id.shootActivity -> {
                    if (isMagnatoMeterAvailable()) {
                        continueShoot()
                    } else {
                        NoMagnaotoMeterDialog().show(
                            supportFragmentManager,
                            "NoMagnaotoMeterDialog"
                        )
                    }
                }

                R.id.completedOrdersFragment -> {
                    if (getString(R.string.app_name) == AppConstants.SPYNE_AI) {
                        intent.putExtra("TAB_ID", 0)
                        setCurrentFragment(myOrdersFragment)
                    } else {
                        val intent = Intent(this, MyOrdersActivity::class.java)
                        intent.putExtra("TAB_ID", 0)
                        startActivity(intent)
                    }
                }
                R.id.wallet -> setCurrentFragment(SecondFragment)
                R.id.logoutDashBoardFragment -> setCurrentFragment(thirdFragment)
            }
            true
        }

        binding.bottomNavigation.selectedItemId = R.id.homeDashboardFragment

        if (intent.getBooleanExtra(AppConstants.IS_NEW_USER, false)) {
            viewModel!!.isNewUser.value = intent.getBooleanExtra(AppConstants.IS_NEW_USER, false)
            viewModel!!.creditsMessage.value = intent.getStringExtra(AppConstants.CREDITS_MESSAGE)
        }

        checkAppVersion()
        observeAppVersion()

        viewModel?.continueAnyway?.observe(this,{
            if (it){
                continueShoot()
            }
        })
    }

    private fun continueShoot() {
        when (getString(R.string.app_name)) {
            "Ola Cabs",
            AppConstants.CARS24,
            AppConstants.CARS24_INDIA,
            AppConstants.SELL_ANY_CAR,
            "Trusted cars",
            "Travo Photos",
            "Yalla Motors",
            "Spyne Hiring",
            AppConstants.AUTO_FOTO -> {
                var intent = Intent(this, StartShootActivity::class.java)
                intent.putExtra(
                    AppConstants.CATEGORY_ID,
                    AppConstants.CARS_CATEGORY_ID
                )
                intent.putExtra(AppConstants.CATEGORY_NAME, "Automobiles")
                startActivity(intent)
            }

            AppConstants.KARVI -> {
                var intent = Intent(this, ShootActivity::class.java)
                intent.putExtra(
                    AppConstants.CATEGORY_ID,
                    AppConstants.CARS_CATEGORY_ID
                )
                intent.putExtra(AppConstants.CATEGORY_NAME, "Automobiles")
                startActivity(intent)
            }

            "Flipkart",
            "Udaan",
            "Lal10",
            "Amazon",
            "Swiggy",
            AppConstants.SWIGGYINSTAMART,
            AppConstants.BATA,
            AppConstants.FLIPKART_GROCERY, AppConstants.EBAY -> {
                val intent =
                    Intent(
                        this@MainDashboardActivity,
                        CategoriesActivity::class.java
                    )
                startActivity(intent)
            }
            else -> {
                var intent = Intent(this, ShootActivity::class.java)
                intent.putExtra(
                    AppConstants.CATEGORY_ID,
                    AppConstants.CARS_CATEGORY_ID
                )
                intent.putExtra(AppConstants.CATEGORY_NAME, "Automobiles")
                startActivity(intent)
            }
        }
    }

    private fun checkAppVersion() {
        if (BuildConfig.VERSION_NAME.contains("debug")) {
            if (allPermissionsGranted()) {
                onPermissionGranted()
            } else {
                permissionRequest.launch(permissions.toTypedArray())
            }
        } else {
            Utilities.showProgressDialog(this)

            viewModel?.getVersionStatus(
                Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(),
                BuildConfig.VERSION_NAME
            )
        }
    }

    private fun observeAppVersion() {
        viewModel?.versionResponse?.observe(this, {
            when (it) {
                is Resource.Success -> {
                    capture(
                        Events.GOT_VERSION
                    )

                    Utilities.hideProgressDialog()
                    if (allPermissionsGranted()) {
                        onPermissionGranted()
                    } else {
                        permissionRequest.launch(permissions.toTypedArray())
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    if (it.errorCode == 400) {
                        //show update app dialog
                        OutdatedVersionDialog().show(
                            supportFragmentManager,
                            "OutdatedVersionDialog"
                        )
                    } else {
                        handleApiError(it) { checkAppVersion() }
                    }
                }
            }
        })
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

            val requiredPermissions = if (getString(R.string.app_name) == AppConstants.OLA_CABS) {
                permissions
            } else {
                permissions.filter {
                    it.key != Manifest.permission.ACCESS_COARSE_LOCATION
                }
            }

            if (requiredPermissions.all {
                    it.value
                }) {
                onPermissionGranted()
            } else {
                RequiredPermissionDialog().show(supportFragmentManager, "RequiredPermissionDialog")
            }

        }

    private fun capture(eventName: String) {
        val properties = HashMap<String,Any?>()
        properties.apply {
            this["email"] =
                Utilities.getPreference(this@MainDashboardActivity, AppConstants.EMAIL_ID)
                    .toString()
        }

        captureEvent(
            eventName,
            properties
        )
    }


    open fun onPermissionGranted() {
        Log.d(
            TAG,
            "onPermissionGranted: " + Utilities.getPreference
                (this, AppConstants.CANCEL_ALL_WROKERS)
        )
        cancelAllWorkers()
        startUploadService()
        startVideoUploadService()
       // checkFolderUpload()
    }

    private fun startVideoUploadService() {
        val shootLocalRepository = VideoLocalRepository()
        if (shootLocalRepository.getOldestVideo().itemId != null
            || shootLocalRepository.getOldestSkippedVideo().itemId != null
        ) {

            var action = Actions.START
            if (getServiceState(this) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
                return

            val serviceIntent = Intent(this, VideoUploadService::class.java)
            serviceIntent.action = action.name

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                ContextCompat.startForegroundService(this, serviceIntent)
                return
            } else {
                log("Starting the service in < 26 Mode")
                startService(serviceIntent)
            }

            val properties = HashMap<String,Any?>()
                .apply {
                    put("service_state", "Started")
                    put(
                        "email",
                        Utilities.getPreference(this@MainDashboardActivity, AppConstants.EMAIL_ID)
                            .toString()
                    )
                    put("medium", "Main Actity")
                }

            captureEvent(Events.VIDEO_SERVICE_STARTED, properties)
        }
    }

    private fun startUploadService() {
        val shootLocalRepository = ShootLocalRepository()
        if (shootLocalRepository.getOldestImage().itemId != null
            || shootLocalRepository.getOldestSkippedImage().itemId != null
        ) {

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

            val properties = HashMap<String,Any?>()
                .apply {
                    put("service_state", "Started")
                    put(
                        "email",
                        Utilities.getPreference(this@MainDashboardActivity, AppConstants.EMAIL_ID)
                            .toString()
                    )
                    put("medium", "Main Actity")
                }

            captureEvent(Events.SERVICE_STARTED, properties)
        }
    }


    private fun cancelAllWorkers() {
        //cancel all workers
        WorkManager.getInstance(this).cancelAllWork()

        WorkManager.getInstance(this).cancelAllWorkByTag("StoreImageFiles  Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("Manual Long Running Worker")
        WorkManager.getInstance(this)
            .cancelAllWorkByTag("Manual Skipped Images Long Running Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("Long Running Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("Skipped Images Long Running Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("Periodic Processing Worker")
        WorkManager.getInstance(this).cancelAllWorkByTag("InternetWorker")

        Utilities.savePrefrence(this, AppConstants.CANCEL_ALL_WROKERS, "Cancelled")
    }

    private fun checkFolderUpload() {
        Utilities.showProgressDialog(this)

        val request = RetrofitClients.buildService(ClipperApi::class.java)
        val authKey = Utilities.getPreference(this, AppConstants.AUTH_KEY).toString()

        val call = request.uploadFolder(authKey)

        call.enqueue(object : Callback<UploadFolderRes> {
            override fun onResponse(
                call: Call<UploadFolderRes>,
                response: Response<UploadFolderRes>
            ) {

                Utilities.hideProgressDialog()

                if (response.isSuccessful) {
                    if (response.body()?.status == 200) {
                        if (response.body()?.data?.isFolderUpload == 1) {
                            if (Utilities.getPreference(
                                    this@MainDashboardActivity,
                                    AppConstants.START_FILES_WORKER
                                ) == ""
                            ) {

                                GlobalScope.launch {
                                    StoreImageFiles(
                                        this@MainDashboardActivity,
                                        ShootRepository(),
                                        FilesRepository()
                                    ).startWork()
                                }

                                capture(Events.FILE_READ_WORKED_INTIATED)
                            } else {
                                //check for maual upload service check
                                startMaunaulUplaod()
                                capture(Events.FILE_WORKER_ALREADY_INTIATED)
                            }
                        } else {
                            capture(Events.FILE_FOLDER_UPLOAD_FALSE)
                        }
                    } else {
                        val properties = HashMap<String,Any?>()
                        properties.apply {
                            this["email"] = Utilities.getPreference(
                                this@MainDashboardActivity,
                                AppConstants.EMAIL_ID
                            ).toString()
                            this["error"] = response.body()?.message
                        }

                        captureEvent(
                            Events.CHECK_FOLDER_API_FAILED,
                            properties
                        )
                    }
                } else {
                    val properties = HashMap<String,Any?>()
                    properties.apply {
                        this["email"] = Utilities.getPreference(
                            this@MainDashboardActivity,
                            AppConstants.EMAIL_ID
                        ).toString()
                        this["error"] = response.errorBody().toString()
                    }

                    captureEvent(
                        Events.CHECK_FOLDER_API_FAILED,
                        properties
                    )
                }
            }

            override fun onFailure(call: Call<UploadFolderRes>, t: Throwable) {
                Utilities.hideProgressDialog()

                val properties = HashMap<String,Any?>()
                properties.apply {
                    this["email"] =
                        Utilities.getPreference(this@MainDashboardActivity, AppConstants.EMAIL_ID)
                            .toString()
                    this["error"] = t.localizedMessage
                }

                captureEvent(
                    Events.CHECK_FOLDER_API_FAILED,
                    properties
                )


                //folderCheckError(t.localizedMessage)
            }
        })
    }

    private fun startMaunaulUplaod() {
        val filesRepository = FilesRepository()
        if (filesRepository.getOldestImage().itemId != null
            || filesRepository.getOldestSkippedImage().itemId != null
        ) {

            var action = Actions.START
            if (getServiceState(this) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
                return

            val serviceIntent = Intent(this, ManualUploadService::class.java)
            serviceIntent.action = action.name

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                ContextCompat.startForegroundService(this, serviceIntent)
                return
            } else {
                log("Starting the service in < 26 Mode")
                startService(serviceIntent)
            }

            val properties = HashMap<String,Any?>()
                .apply {
                    put("service_state", "Started")
                    put(
                        "email",
                        Utilities.getPreference(this@MainDashboardActivity, AppConstants.EMAIL_ID)
                            .toString()
                    )
                    put("medium", "Main Activity")
                }

            captureEvent(Events.SERVICE_STARTED, properties)
        }
    }


    private fun folderCheckError(error: String) {
        Snackbar.make(binding.root, error, Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry") {
                //checkFolderUpload()
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.primary))
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel?.resultCode != 0 && viewModel?.resultCode != -1) {
            binding.bottomNavigation.selectedItemId = R.id.homeDashboardFragment
        }
        viewModel?.resultCode = null
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(binding.flContainer.id, fragment)
            commit()
        }

    override fun onBackPressed() {
        if (binding.bottomNavigation.selectedItemId != R.id.homeDashboardFragment)
            binding.bottomNavigation.selectedItemId = R.id.homeDashboardFragment
        else
            super.onBackPressed()
    }
}
