package com.spyneai.processedimages.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityProcessedImageBinding
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.reshoot.ui.SelectImagesFragment

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
                .add(binding.flContainer.id,SelectImagesFragment())
                .commit()
        })
    }
}