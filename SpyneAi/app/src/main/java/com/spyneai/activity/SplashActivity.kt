package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.installations.Utils
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (Utilities.getPreference(this,AppConstants.LANGUAGE).equals("English"))
        {
            llLanguage.visibility= View.GONE

            Handler().postDelayed({
                if (Utilities.getPreference(this, AppConstants.tokenId).isNullOrEmpty())
                {
                    val intent = Intent(this, OnboardOneActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }, 3000) // 3000 is the delayed time in milliseconds.
        }
        else {
            setSplash();
            llLanguage.visibility= View.VISIBLE
        }
    }

    //Start splash
    private fun setSplash() {
        llEnglish.setOnClickListener(View.OnClickListener {
            if (Utilities.getPreference(this, AppConstants.tokenId).isNullOrEmpty())
            {
                val intent = Intent(this, OnboardOneActivity::class.java)
                Utilities.savePrefrence(this,AppConstants.LANGUAGE,"English")
                startActivity(intent)
                finish()
            }
            else {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        llHindi.setOnClickListener(View.OnClickListener {
            Toast.makeText(this,
                "Coming soon...",
                Toast.LENGTH_SHORT).show()
        })
    }
}