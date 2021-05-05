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
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_splash.*
import okhttp3.OkHttpClient


class SplashActivity : AppCompatActivity() {
    private val MY_REQUEST_CODE: Int = 1
    val appUpdateManager = AppUpdateManagerFactory.create(this)

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

        setAnimation()
//        setSplash();
        autoUpdates()

    }

    private fun setAnimation() {
        val animTogether = AnimationUtils.loadAnimation(this, R.anim.together);
        ivLogoSpyne.startAnimation(animTogether)
    }

    private fun autoUpdates() {


        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE
                )
            } else {
                setSplash();
            }
        }

    }

    //Start splash
    private fun setSplash() {
        Handler().postDelayed({
            if (Utilities.getPreference(this, AppConstants.tokenId).isNullOrEmpty()) {
                val intent = Intent(this, OnboardingsActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000)
    }

    //    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == MY_REQUEST_CODE) {
//            if (resultCode != RESULT_OK) {
//                Log.e("UPdate", "Update flow failed! Result code: $resultCode")
//                // If the update is cancelled or fails,
//                // you can request to start the update again.
//            }
//            else{
//               // setSplash()
//            }
//
//        }
//    }

    // Checks that the update is not stalled during 'onResume()'.
// However, you should execute this check at all entry points into the app.
    override fun onResume() {
        super.onResume()

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    )
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e("MY_APP", "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
            else{
                setSplash()
            }
        }
    }
}