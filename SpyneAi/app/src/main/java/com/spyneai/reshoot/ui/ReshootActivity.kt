package com.spyneai.reshoot.ui

import CameraFragment
import android.content.Intent
import android.content.pm.ActivityInfo
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
import com.spyneai.shoot.ui.dialogs.ShootExitDialog

class ReshootActivity : AppCompatActivity() {

    lateinit var shootViewModel: ShootViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(R.layout.activity_reshoot)
        setLocale()

        shootViewModel = ViewModelProvider(this, ViewModelFactory()).get(ShootViewModel::class.java)
        shootViewModel.isReshoot = true

        val categoryDetails = CategoryDetails()

        categoryDetails.apply {
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)
            gifList = intent.getStringExtra(AppConstants.GIF_LIST)
        }

        shootViewModel.categoryDetails.value = categoryDetails

        setShoot()

        val transaction = supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, CameraFragment())

        when (categoryDetails.categoryId) {
            AppConstants.FOOTWEAR_CATEGORY_ID -> {
                transaction.add(R.id.flContainer, EcomOverlayReshootFragment())
                    .commit()
            }
            else -> {
                transaction.add(R.id.flContainer, ReshootFragment())
                    .commit()
            }
        }

        shootViewModel.reshootCompleted.observe(this, {
            Intent(this, ReshootDoneActivity::class.java)
                .apply {
                    putExtra(AppConstants.CATEGORY_ID,categoryDetails.categoryId)
                    startActivity(this)
                }
        })
    }

    private fun setShoot() {
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

        val sku = Sku()
        sku.projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
        sku.skuId = intent.getStringExtra(AppConstants.SKU_ID)
        sku.skuName = intent.getStringExtra(AppConstants.SKU_NAME)

        shootViewModel.sku.value = sku
    }

    override fun onBackPressed() {
        ShootExitDialog().show(supportFragmentManager, "ShootExitDialog")
    }
}