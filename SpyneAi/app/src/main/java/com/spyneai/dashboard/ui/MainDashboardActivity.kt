package com.spyneai.dashboard.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityDashboardMainBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.StartShootActivity


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

        val firstFragment=HomeDashboardFragment()
        val SecondFragment=WalletDashboardFragment()
        val thirdFragment=LogoutDashBoardFragment()

        //save category id and name
        Utilities.savePrefrence(this,AppConstants.CATEGORY_ID,AppConstants.CARS_CATEGORY_ID)
        Utilities.savePrefrence(this,AppConstants.CATEGORY_NAME,"Automobiles")


        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.homeDashboardFragment->setCurrentFragment(firstFragment)

                R.id.shootActivity-> {
                    var intent =
                        when(getString(R.string.app_name)) {
                        "Ola Cabs",WhiteLabelConstants.CARS24,WhiteLabelConstants.CARS24_INDIA,"Trusted cars","Travo Photos","Yalla Motors","Spyne Hiring" ->  Intent(this, StartShootActivity::class.java)
                            else -> Intent(this, ShootActivity::class.java)
                    }

                    intent.putExtra(AppConstants.CATEGORY_ID,AppConstants.CARS_CATEGORY_ID)
                    intent.putExtra(AppConstants.CATEGORY_NAME,"Automobiles")
                    startActivity(intent)
                }
                R.id.completedOrdersFragment-> {
                    val intent = Intent(this, MyOrdersActivity::class.java)
                    startActivity(intent)
                }
                //R.id.wallet->setCurrentFragment(SecondFragment)
                R.id.logoutDashBoardFragment->setCurrentFragment(thirdFragment)

            }
            true
        }

        if (intent.getBooleanExtra(AppConstants.IS_NEW_USER,false)){
            viewModel.isNewUser.value = intent.getBooleanExtra(AppConstants.IS_NEW_USER,false)
            viewModel.creditsMessage.value = intent.getStringExtra(AppConstants.CREDITS_MESSAGE)
        }
    }



    override fun onResume() {
        super.onResume()

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