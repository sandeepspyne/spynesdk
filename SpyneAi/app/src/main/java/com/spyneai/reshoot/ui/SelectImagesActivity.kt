package com.spyneai.reshoot.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R

class SelectImagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_images)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, SelectImagesFragment())
            .commit()
    }
}