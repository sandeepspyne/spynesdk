package com.spyneai.reshoot.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.ui.base.ImageProcessingStartedFragment

class ReshootDoneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reshoot_done)

        val bundle = Bundle()
        bundle.putString(AppConstants.CATEGORY_ID,intent.getStringExtra(AppConstants.CATEGORY_ID))

        val fragment = ImageProcessingStartedFragment()
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, fragment)
            .commit()
    }

    override fun onBackPressed() {

    }
}