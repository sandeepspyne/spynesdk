package com.spyneai.dashboard.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.spyneai.R
import com.spyneai.activity.CategoriesActivity
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import kotlinx.android.synthetic.main.activity_dashboard_main.*


class MainDashboardActivity : AppCompatActivity() {

    lateinit var navHostFragment: NavHostFragment

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment

        navController = navHostFragment.navController

        bottomNavigation.background = null
        bottomNavigation.menu.getItem(2).isEnabled = false
        bottomNavigation.setupWithNavController(navController)

        val viewModel = ViewModelProvider(this, ViewModelFactory()).get(DashboardViewModel::class.java)
        if (intent.getBooleanExtra(AppConstants.IS_NEW_USER,false)){
            viewModel.isNewUser.value = intent.getBooleanExtra(AppConstants.IS_NEW_USER,false)
            viewModel.creditsMessage.value = intent.getStringExtra(AppConstants.CREDITS_MESSAGE)
        }

        fab.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

}