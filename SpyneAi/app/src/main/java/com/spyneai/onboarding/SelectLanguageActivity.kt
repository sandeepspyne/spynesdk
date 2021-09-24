package com.spyneai.onboarding

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.spyneai.R
import com.spyneai.databinding.ActivitySelectLanguageActivityBinding
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import java.util.*

class SelectLanguageActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectLanguageActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLanguageActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.llEnglish.setOnClickListener {
            onLanguageSelected("en")
        }

        binding.llGermany.setOnClickListener {
            onLanguageSelected("de")
        }

        binding.llItly.setOnClickListener {
            onLanguageSelected("IT")
        }

    }

    private fun onLanguageSelected(locale: String) {

        Utilities.savePrefrence(this, AppConstants.LOCALE,locale)

        val locale = Locale(locale)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)

        Intent(this,LoginActivity::class.java)
            .apply {
                startActivity(this)
            }

        finish()
    }
}