package com.spyneai.dashboard.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.activity.CategoriesActivity
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityDashboardMainBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.StartShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity


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
                            "Flipkart", "Udaan", "Lal10", "Amazon" -> {
//                            var intent = Intent(this, ShootPortraitActivity::class.java)
//                            if (Utilities.getPreference(this@MainDashboardActivity, AppConstants.CATEGORY_NAME).equals("Footwear")){
//                                intent.putExtra(AppConstants.CATEGORY_ID,AppConstants.FOOTWEAR_CATEGORY_ID)
//                                intent.putExtra(AppConstants.CATEGORY_NAME,"Footwear")
//                                Utilities.savePrefrence(this@MainDashboardActivity, AppConstants.CATEGORY_ID, AppConstants.FOOTWEAR_CATEGORY_ID)
//                                Utilities.savePrefrence(this@MainDashboardActivity, AppConstants.CATEGORY_NAME, "Footwear")
//                            }else if(Utilities.getPreference(this@MainDashboardActivity, AppConstants.CATEGORY_NAME).equals("E-Commerce")){
//                                intent.putExtra(AppConstants.CATEGORY_ID,AppConstants.ECOM_CATEGORY_ID)
//                                intent.putExtra(AppConstants.CATEGORY_NAME,"E-Commerce")
//                                Utilities.savePrefrence(this@MainDashboardActivity, AppConstants.CATEGORY_ID, AppConstants.ECOM_CATEGORY_ID)
//                                Utilities.savePrefrence(this@MainDashboardActivity, AppConstants.CATEGORY_NAME, "E-Commerce")
//                            }
//                            startActivity(intent)

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
               // R.id.wallet->setCurrentFragment(SecondFragment)
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