package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.installations.Utils
import com.spyneai.R
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.model.otp.OtpResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_splash.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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