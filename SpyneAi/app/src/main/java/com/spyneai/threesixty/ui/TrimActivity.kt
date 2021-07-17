package com.spyneai.threesixty.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityTrimBinding
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class TrimActivity : AppCompatActivity() {

    lateinit var binding : ActivityTrimBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrimBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val threeSixtyViewModel = ViewModelProvider(this, ViewModelFactory()).get(ThreeSixtyViewModel::class.java)

        threeSixtyViewModel.videoDetails.apply {
            videoPath = intent.getStringExtra("src_path")
            skuId = intent.getStringExtra("sku_id")
            skuName = intent.getStringExtra("sku_name")
            projectId = intent.getStringExtra("project_id")
            shootMode = intent.getIntExtra("shoot_mode",0)
        }
    }
}