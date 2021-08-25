package com.spyneai.dashboard.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.spyneai.BaseApplication
import com.spyneai.R
import com.spyneai.activity.CategoriesActivity
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityDashboardMainBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.StartShootActivity
import com.spyneai.shoot.workmanager.manual.StoreImageFilesWorker
import java.io.File

import com.google.android.material.snackbar.Snackbar



class MainDashboardActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDashboardMainBinding
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

        val viewModel = ViewModelProvider(this, ViewModelFactory()).get(DashboardViewModel::class.java)

        val firstFragment= HomeDashboardFragment()
        val SecondFragment=WalletDashboardFragment()
        val thirdFragment=LogoutDashBoardFragment()

        //save category id and name
        Utilities.savePrefrence(this,AppConstants.CATEGORY_ID,AppConstants.CARS_CATEGORY_ID)
        Utilities.savePrefrence(this,AppConstants.CATEGORY_NAME,"Automobiles")


        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.homeDashboardFragment->setCurrentFragment(firstFragment)

                R.id.shootActivity-> {

                        when(getString(R.string.app_name)) {
                        "Ola Cabs", AppConstants.CARS24,AppConstants.CARS24_INDIA,
                        "Trusted cars","Travo Photos","Yalla Motors","Spyne Hiring" -> {
                            var intent = Intent(this, StartShootActivity::class.java)
                            intent.putExtra(AppConstants.CATEGORY_ID,AppConstants.CARS_CATEGORY_ID)
                            intent.putExtra(AppConstants.CATEGORY_NAME,"Automobiles")
                            startActivity(intent)

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
                    val intent = Intent(this, MyOrdersActivity::class.java)
                    startActivity(intent)
                }
                R.id.wallet->setCurrentFragment(SecondFragment)
                R.id.logoutDashBoardFragment->setCurrentFragment(thirdFragment)

            }
            true
        }

        if (intent.getBooleanExtra(AppConstants.IS_NEW_USER,false)){
            viewModel.isNewUser.value = intent.getBooleanExtra(AppConstants.IS_NEW_USER,false)
            viewModel.creditsMessage.value = intent.getStringExtra(AppConstants.CREDITS_MESSAGE)
        }

        if (getString(R.string.app_name) == AppConstants.OLA_CABS){
            if (allPermissionsGranted()) {
                onPermissionGranted()
            } else {
                permissionRequest.launch(permissions.toTypedArray())
            }
        }
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
        } else {
            Snackbar.make(binding.root, "App cannot work without permission", Snackbar.LENGTH_INDEFINITE)
                .setAction("Allow") {
                   requestPermi()
                }
                .setActionTextColor(ContextCompat.getColor(this,R.color.primary))
                .show()
        }
    }

    private fun requestPermi() {
        permissionRequest.launch(permissions.toTypedArray())
    }

    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }


    open fun onPermissionGranted(){
        val storeWorkRequest = OneTimeWorkRequest.Builder(StoreImageFilesWorker::class.java)

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                storeWorkRequest
                    .build())
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