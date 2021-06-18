package com.spyneai.shoot.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.Sku

class ProcessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process)

        val processViewModel = ViewModelProvider(this, ViewModelFactory()).get(ProcessViewModel::class.java)

        val sku = Sku()
        sku.skuId = intent.getStringExtra("sku_id")
        processViewModel.sku.value = sku

        processViewModel.exteriorAngles.value =  intent.getIntExtra("exterior_angles",0)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, SelectBackgroundFragment())
            .commit()

        processViewModel.startTimer.observe(this,{
            if (it) {
                // add select background fragment
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, TimerFragment())
                    .commit()
            }
        })

    }
}