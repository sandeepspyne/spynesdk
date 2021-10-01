package com.spyneai.reshoot.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.R

class ReshootActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reshoot)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, ReshootFragment())
            .commit()
    }
}