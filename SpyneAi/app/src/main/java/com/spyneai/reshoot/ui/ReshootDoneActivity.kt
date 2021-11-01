package com.spyneai.reshoot.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R
import com.spyneai.shoot.ui.base.ImageProcessingStartedFragment

class ReshootDoneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reshoot_done)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, ImageProcessingStartedFragment())
            .commit()
    }

    override fun onBackPressed() {

    }
}