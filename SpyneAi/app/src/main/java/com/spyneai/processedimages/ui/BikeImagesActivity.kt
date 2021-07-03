package com.spyneai.processedimages.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityBikeImagesBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.shoot.data.ShootViewModel

class BikeImagesActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBikeImagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBikeImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val processedViewModel = ViewModelProvider(this, ViewModelFactory()).get(ProcessedViewModel::class.java)
        processedViewModel.skuId = intent.getStringExtra(AppConstants.SKU_ID)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer,BikeImagesFragment())
            .commit()

        binding.ivBackShowImages.setOnClickListener {
            onBackPressed()
        }

        binding.ivHomeShowImages.setOnClickListener {
            gotoHome()
        }
    }
}