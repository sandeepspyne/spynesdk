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

import android.util.Log

import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.spyneai.dashboard.ui.dashboard.MainDashboardActivity
import kotlinx.android.synthetic.main.activity_splash.*
import okhttp3.OkHttpClient


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        Stetho.initializeWithDefaults(this);
        OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()
        setSplash();
    }

    private fun setAnimation() {
        val animTogether = AnimationUtils.loadAnimation(this, R.anim.together);
        ivLogoSpyne.startAnimation(animTogether)
    }

    //Start splash
    private fun setSplash() {
        Handler().postDelayed({
            if (Utilities.getPreference(this, AppConstants.tokenId).isNullOrEmpty()) {
                val intent = Intent(this, OnboardingsActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, MainDashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000)
    }
}