package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.spyneai.OnboardTwoActivity
import com.spyneai.R
import kotlinx.android.synthetic.main.activity_onboard_one.*


class OnboardOneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard_one)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

       // startAnim()

        tv_get_started.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, OnboardTwoActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0);
        })

    }

    private fun startAnim() {
        ivOne.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        System.exit(0)
    }
}