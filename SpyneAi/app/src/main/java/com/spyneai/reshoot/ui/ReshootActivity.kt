package com.spyneai.reshoot.ui

import CameraFragment
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.setLocale
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.CategoryDetails
import com.spyneai.shoot.data.model.CreateProjectRes
import com.spyneai.shoot.data.model.Sku

class ReshootActivity : AppCompatActivity() {

    lateinit var shootViewModel: ShootViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_reshoot)
        setLocale()

        shootViewModel = ViewModelProvider(this, ViewModelFactory()).get(ShootViewModel::class.java)

        val categoryDetails = CategoryDetails()

        categoryDetails.apply {
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)
            gifList =  intent.getStringExtra(AppConstants.GIF_LIST)
        }

        shootViewModel.categoryDetails.value = categoryDetails

        setUpVideoShoot()

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, CameraFragment())
            .add(R.id.flContainer, ReshootFragment())
            .commit()
    }

    private fun setUpVideoShoot() {
        shootViewModel.getSubCategories(
            Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(),
            intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )

        shootViewModel.isProjectCreated.value = true
        shootViewModel.projectId.value = intent.getStringExtra(AppConstants.PROJECT_ID)

        shootViewModel._createProjectRes.value = Resource.Success(
            CreateProjectRes(
                "",
                intent.getStringExtra(AppConstants.PROJECT_ID)!!,
                200
            )
        )

        shootViewModel.getOverlays(
            Utilities.getPreference(this,AppConstants.AUTH_KEY).toString(),
            intent.getStringExtra(AppConstants.CATEGORY_ID)!!,
            intent.getStringExtra(AppConstants.SUB_CAT_ID)!!,
            intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0).toString(),
        )

        val sku = Sku()
        sku?.projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
        sku?.skuId = intent.getStringExtra(AppConstants.SKU_ID)
        sku?.skuName = intent.getStringExtra(AppConstants.SKU_NAME)
        //sku?.totalImages = viewModel.exterirorAngles.value
       // sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
        //sku?.subcategoryId = prod_sub_cat_id
       // sku?.exteriorAngles = viewModel.exterirorAngles.value

        shootViewModel.sku.value = sku


    }
}