package com.spyneai.threesixty.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityThreeSixtyIntroBinding
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.setLocale
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.threesixty.data.model.VideoDetails

class ThreeSixtyIntroActivity : AppCompatActivity() {

    lateinit var binding: ActivityThreeSixtyIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThreeSixtyIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLocale()

        val threeSixtyViewModel = ViewModelProvider(this, ViewModelFactory()).get(ThreeSixtyViewModel::class.java)

        val videoDetails = VideoDetails(
            uuid = getUuid(),
            projectUuid = getUuid(),
            skuUuid = getUuid(),
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!,
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)!!
        )

        //set category name and id
        threeSixtyViewModel.videoDetails = videoDetails

        threeSixtyViewModel.title.observe(this,{
            binding.tvTitle.text = it
        })

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

}