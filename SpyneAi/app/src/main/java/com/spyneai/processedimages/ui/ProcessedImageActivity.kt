package com.spyneai.processedimages.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityDashboardMainBinding
import com.spyneai.databinding.ActivityProcessedImageBinding
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.reshoot.ui.ReshootFragment
import com.spyneai.shoot.data.ProcessViewModel

class ProcessedImageActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProcessedImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessedImageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportFragmentManager
            .beginTransaction()
            .add(binding.flContainer.id,ProcessedImagesFragment())
            .commit()

        val processViewModel = ViewModelProvider(this, ViewModelFactory()).get(ProcessedViewModel::class.java)

        processViewModel.reshoot.observe(this,{
            supportFragmentManager
                .beginTransaction()
                .add(binding.flContainer.id,ReshootFragment())
                .commit()
        })
    }
}