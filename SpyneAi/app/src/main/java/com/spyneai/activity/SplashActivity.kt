package com.spyneai.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import java.util.*


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)


        val locale = Locale("de")
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)

        setSplash()
    }


    //Start splash
    private fun setSplash() {
        Handler().postDelayed({
            if (Utilities.getPreference(this, AppConstants.AUTH_KEY).isNullOrEmpty()) {
                var intent: Intent? = null

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