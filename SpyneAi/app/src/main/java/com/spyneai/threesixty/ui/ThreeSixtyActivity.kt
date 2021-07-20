package com.spyneai.threesixty.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityThreeSixtyBinding
import com.spyneai.needs.AppConstants
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtyActivity : AppCompatActivity() {

    lateinit var binding : ActivityThreeSixtyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = ActivityThreeSixtyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val threeSixtyViewModel = ViewModelProvider(this, ViewModelFactory()).get(ThreeSixtyViewModel::class.java)

        threeSixtyViewModel.videoDetails.apply {
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
            frames =  intent.getIntExtra("frames",0)
        }

        supportFragmentManager.beginTransaction()
            .add(binding.flContainer.id,RecordVideoFragment())
            .add(binding.flContainer.id,SubcategoriesFragment())
            .commit()

    }
}