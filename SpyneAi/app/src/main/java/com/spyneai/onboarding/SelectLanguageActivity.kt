package com.spyneai.onboarding

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.R
import com.spyneai.databinding.ActivitySelectLanguageActivityBinding
import com.spyneai.loginsignup.activity.LoginActivity
import java.util.*

class SelectLanguageActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectLanguageActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLanguageActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.llGermany.setOnClickListener {
            onLanguageSelected("de")
        }

    }

    private fun onLanguageSelected(locale: String) {
        val locale = Locale(locale)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)

        Intent(this,LoginActivity::class.java)
            .apply {
                startActivity(this)
            }
    }
}