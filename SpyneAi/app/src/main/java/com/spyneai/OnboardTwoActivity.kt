package com.spyneai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.spyneai.activity.OnboardThreeActivity
import kotlinx.android.synthetic.main.activity_onboard_one.*
import kotlinx.android.synthetic.main.activity_onboard_one.tv_get_started
import kotlinx.android.synthetic.main.activity_onboard_two.*

class OnboardTwoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard_two)

        tv_get_started.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, OnboardThreeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0);

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(0, 0);

    }
}