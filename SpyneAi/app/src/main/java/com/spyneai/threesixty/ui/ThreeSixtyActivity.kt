package com.spyneai.threesixty.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityThreeSixtyBinding
import com.spyneai.databinding.ActivityThreeSixtyViewBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.CategoryDetails
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.threesixty.ui.dialogs.ThreeSixtyExteriorGifDialog

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

        val categoryDetails = CategoryDetails()

        categoryDetails.apply {
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)
            gifList =  intent.getStringExtra(AppConstants.GIF_LIST)
        }

        threeSixtyViewModel.categoryDetails.value = categoryDetails

        supportFragmentManager.beginTransaction()
            .add(binding.flContainer.id,RecordVideoFragment())
            .add(binding.flContainer.id,SubcategoriesFragment())
            .commit()

    }
}