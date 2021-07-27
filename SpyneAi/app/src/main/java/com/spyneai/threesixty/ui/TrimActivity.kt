package com.spyneai.threesixty.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityTrimBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.dialogs.ShootExitDialog
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class TrimActivity : AppCompatActivity() {

    lateinit var binding : ActivityTrimBinding
    lateinit var threeSixtyViewModel : ThreeSixtyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrimBinding.inflate(layoutInflater)
        setContentView(binding.root)

        threeSixtyViewModel = ViewModelProvider(this, ViewModelFactory()).get(ThreeSixtyViewModel::class.java)

        threeSixtyViewModel.videoDetails.apply {
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
            videoPath = intent.getStringExtra("src_path")
            skuId = intent.getStringExtra("sku_id")
            skuName = intent.getStringExtra("sku_name")
            projectId = intent.getStringExtra("project_id")
            frames = intent.getIntExtra("frames",0)
            shootMode = intent.getIntExtra("shoot_mode",0)
        }

        threeSixtyViewModel.title.observe(this,{
            binding.tvTitle.text = it
        })

        threeSixtyViewModel.processingStarted.observe(this,{
            if (it) binding.ivBack.visibility = View.INVISIBLE
        })

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (threeSixtyViewModel.processingStarted.value == null || threeSixtyViewModel.processingStarted.value == false)
            ShootExitDialog().show(supportFragmentManager,"ShootExitDialog")
    }
}