package com.spyneai.processedimages.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.R
import com.spyneai.databinding.ActivityDashboardMainBinding
import com.spyneai.databinding.ActivityProcessedImageBinding

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

    }
}