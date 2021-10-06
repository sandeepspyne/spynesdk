package com.spyneai.extras

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R

class OnboardTwoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard_two)

/*
        tv_get_started.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, OnboardThreeActivity::class.java)
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