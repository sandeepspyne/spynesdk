package com.spyneai.shoot.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.ui.dialogs.ShootExitDialog

class ProcessActivity : AppCompatActivity() {

    lateinit var processViewModel : ProcessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process)

        processViewModel = ViewModelProvider(this, ViewModelFactory()).get(ProcessViewModel::class.java)

        val sku = Sku()
        sku.skuId = intent.getStringExtra("sku_id")
        processViewModel.sku.value = sku

        processViewModel.exteriorAngles.value =  intent.getIntExtra("exterior_angles",0)
        processViewModel.categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)

        when(intent.getStringExtra(AppConstants.CATEGORY_NAME)) {
            "Automobiles" -> {
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, SelectBackgroundFragment())
                    .commit()
            }
            "Bikes" -> {
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, ImageProcessingStartedFragment())
                    .commit()
            }
        }


        processViewModel.startTimer.observe(this,{
            if (it) {
                // add select background fragment
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, ImageProcessingStartedFragment())
                    .commit()
            }
        })
    }

    override fun onBackPressed() {
        if (processViewModel.startTimer.value == null || !processViewModel.startTimer.value!!)
            ShootExitDialog().show(supportFragmentManager,"ShootExitDialog")
    }
}