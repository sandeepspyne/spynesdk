package com.spyneai.shoot.ui.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.setLocale
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.ui.RegularShootSummaryFragment
import com.spyneai.shoot.ui.SelectBackgroundFragment
import com.spyneai.shoot.ui.dialogs.ShootExitDialog

class ProcessActivity : AppCompatActivity() {

    val TAG = "ProcessActivity"
    lateinit var processViewModel : ProcessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process)

        setLocale()

        processViewModel = ViewModelProvider(this, ViewModelFactory()).get(ProcessViewModel::class.java)

        processViewModel.fromVideo = intent.getBooleanExtra(AppConstants.FROM_VIDEO,false)

        val sku = Sku()
        sku.skuId = intent.getStringExtra("sku_id")
        sku.projectId = intent.getStringExtra("project_id")
        processViewModel.sku.value = sku

        processViewModel.exteriorAngles.value =  intent.getIntExtra("exterior_angles",0)
        processViewModel.interiorMiscShootsCount = intent.getIntExtra("interior_misc_count",0)
        processViewModel.frontFramesList = intent.getStringArrayListExtra("exterior_images_list")!!
        processViewModel.categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)

        Log.d(TAG, "onCreate: "+processViewModel.exteriorAngles.value)
        Log.d(TAG, "onCreate: "+processViewModel.interiorMiscShootsCount)


//        if (processViewModel.categoryName == "Automobiles" || processViewModel.categoryName == "Bikes")
//            processViewModel.frontFramesList = intent.getStringArrayListExtra("exterior_images_list")!!

        if (intent.getBooleanExtra("process_sku",true)){
            supportFragmentManager.beginTransaction()
                .add(R.id.flContainer, SelectBackgroundFragment())
                .commit()
        }else{
           processViewModel.startTimer.value = true
        }

        processViewModel.startTimer.observe(this,{
            if (it) {
                // add select background fragment
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, ImageProcessingStartedFragment())
                    .commit()
            }
        })

        processViewModel.addRegularShootSummaryFragment.observe(this,{
            if (it) {
                // add select background fragment
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, RegularShootSummaryFragment())
                    .addToBackStack("RegularShootSummaryFragment")
                    .commit()

                processViewModel.addRegularShootSummaryFragment.value = false
            }
        })
    }

    override fun onBackPressed() {
        if (processViewModel.isRegularShootSummaryActive) {
            processViewModel.isRegularShootSummaryActive = false
            super.onBackPressed()
        }else{
            if (processViewModel.startTimer.value == null || !processViewModel.startTimer.value!!)
                ShootExitDialog().show(supportFragmentManager,"ShootExitDialog")
        }

    }
}