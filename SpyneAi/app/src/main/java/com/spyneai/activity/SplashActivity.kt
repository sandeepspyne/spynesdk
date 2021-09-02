package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.loginsignup.activity.LoginActivity
import okhttp3.OkHttpClient


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        setSplash()
    }


    //Start splash
    private fun setSplash() {
        Handler().postDelayed({
            if (Utilities.getPreference(this, AppConstants.AUTH_KEY).isNullOrEmpty()) {
                var intent : Intent? = null

                intent = when(getString(R.string.app_name)) {
                    AppConstants.SPYNE_AI->  Intent(this,
                        OnboardingsActivity::class.java)

                    else -> Intent(this, LoginActivity::class.java)
                }

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                val intent = Intent(this, MainDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }, 3000)
    }
}