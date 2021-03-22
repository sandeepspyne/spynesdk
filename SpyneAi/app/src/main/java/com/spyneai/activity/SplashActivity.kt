package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        setSplash();
    }

    //Start splash
    private fun setSplash() {
        Handler().postDelayed({
            if (Utilities.getPreference(this, AppConstants.tokenId).isNullOrEmpty())
            {
                val intent = Intent(this, OnboardingsActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000)
    }
}