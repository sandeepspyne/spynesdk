package com.spyneai.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.spyneai.R
import kotlinx.android.synthetic.main.activity_onboard_one.*
import kotlinx.android.synthetic.main.activity_onboard_three.*

class OnboardThreeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard_three)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

/*
        tv_get_started.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0);
        })
*/

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(0, 0);
    }
}