package com.spyneai.threesixty.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityThreeSixtyBinding
import com.spyneai.needs.AppConstants
import com.spyneai.setLocale
import com.spyneai.shoot.ui.dialogs.ShootExitDialog
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtyActivity : AppCompatActivity() {

    lateinit var binding : ActivityThreeSixtyBinding
    lateinit var threeSixtyViewModel : ThreeSixtyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLocale()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = ActivityThreeSixtyBinding.inflate(layoutInflater)
        setContentView(binding.root)


        threeSixtyViewModel = ViewModelProvider(this, ViewModelFactory()).get(ThreeSixtyViewModel::class.java)

        if (intent.getBooleanExtra(AppConstants.FROM_DRAFTS,false))
            setUpDrafts()

        threeSixtyViewModel.setVideoDatils(intent.getStringExtra(AppConstants.VIDEO_UUID)!!)

//        threeSixtyViewModel.videoDetails?.apply {
//            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)!!
//            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
//            frames =  intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0)
//        }

        supportFragmentManager.beginTransaction()
            .add(binding.flContainer.id,RecordVideoFragment())
            .add(binding.flContainer.id,SubcategoriesFragment())
            .commit()
    }

    private fun setUpDrafts() {
        threeSixtyViewModel.fromDrafts = true

        threeSixtyViewModel.videoDetails?.apply {
            projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
            skuName = intent.getStringExtra(AppConstants.SKU_NAME)
            skuId = intent.getStringExtra(AppConstants.SKU_ID)
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
            frames =  intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0)
        }
    }

    override fun onBackPressed() {
        ShootExitDialog().show(supportFragmentManager,
            "ShootExitDialog")
    }
}