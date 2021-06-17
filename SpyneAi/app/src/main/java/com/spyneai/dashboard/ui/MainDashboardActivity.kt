package com.spyneai.dashboard.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.spyneai.R
import com.spyneai.activity.CategoriesActivity
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

//        fab.setImageDrawable(this, R.drawable.ic_shoot)

        fab.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

//        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

}