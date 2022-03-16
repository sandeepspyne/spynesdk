package com.spyneai.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.BuildConfig
import com.spyneai.R
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.getNetworkName
import com.spyneai.loginsignup.OnboardingsActivity
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.onboarding.SelectLanguageActivity
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash)

        when (getString(R.string.app_name)) {
            AppConstants.SPYNE_AI_AUTOMOBILE,AppConstants.SPYNE_AI, AppConstants.AUTO_FOTO ->{
                ivPowredBy.visibility = View.INVISIBLE
            } else -> {
            ivPowredBy.visibility = View.VISIBLE
        } }


        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val version = Build.VERSION.SDK_INT
        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName: String = BuildConfig.VERSION_NAME
        val networkCarrier = getNetworkName()


        Utilities.savePrefrence(this,AppConstants.DEVICE_ID,deviceId)
        Utilities.savePrefrence(this,AppConstants.DEVICE_MANUFACTURER,manufacturer)
        Utilities.savePrefrence(this,AppConstants.MODEL,model)
        Utilities.savePrefrence(this,AppConstants.OS_VERSION,version.toString())
        Utilities.savePrefrence(this,AppConstants.APP_VERSION,versionName)
        Utilities.savePrefrence(this,AppConstants.APP_VERSION_CODE,versionCode.toString())
        Utilities.savePrefrence(this,AppConstants.NETWORK_TYPE,networkCarrier)
        Utilities.savePrefrence(this,AppConstants.DEVICE_ID,deviceId)

        if(Utilities.getPreference(this, AppConstants.STATUS_PROJECT_NAME).isNullOrEmpty()){
            Utilities.savePrefrence(this,AppConstants.STATUS_PROJECT_NAME,"true")
        }

        setSplash()
    }


    //Start splash
    private fun setSplash() {
        Handler().postDelayed({
            if (Utilities.getPreference(this, AppConstants.AUTH_KEY).isNullOrEmpty()) {
                var intent: Intent? = null

                intent = when(getString(R.string.app_name)) {
                    AppConstants.SPYNE_AI->  Intent(this, OnboardingsActivity::class.java)
                    AppConstants.SPYNE_AI_AUTOMOBILE->  Intent(this, OnboardingsActivity::class.java)

                    AppConstants.AUTO_FOTO -> Intent(this, SelectLanguageActivity::class.java)

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