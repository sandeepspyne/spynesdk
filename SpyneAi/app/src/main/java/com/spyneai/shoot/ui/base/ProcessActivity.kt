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
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.ui.RegularShootSummaryFragment
import com.spyneai.shoot.ui.SelectBackgroundFragment
import com.spyneai.shoot.ui.dialogs.ShootExitDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProcessActivity : AppCompatActivity() {

    val TAG = "ProcessActivity"
    lateinit var processViewModel : ProcessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process)

        setLocale()

        processViewModel = ViewModelProvider(this, ViewModelFactory()).get(ProcessViewModel::class.java)

        processViewModel.fromVideo = intent.getBooleanExtra(AppConstants.FROM_VIDEO,false)


        processViewModel.exteriorAngles.value =  intent.getIntExtra("exterior_angles",0)
        processViewModel.interiorMiscShootsCount = intent.getIntExtra("interior_misc_count",0)
        processViewModel.frontFramesList = intent.getStringArrayListExtra("exterior_images_list")!!
        processViewModel.categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)

        val projectUuid = intent.getStringExtra(AppConstants.PROJECT_UUIID)!!
        val skuUUid = intent.getStringExtra(AppConstants.SKU_UUID)!!

        GlobalScope.launch(Dispatchers.IO) {
            processViewModel.setProjectAndSkuData(
                projectUuid,
                skuUUid
            )
        }


        if (intent.getBooleanExtra("process_sku",true)){
            supportFragmentManager.beginTransaction()
                .add(R.id.flContainer, SelectBackgroundFragment())
                .commit()
        }else{
           processViewModel.startTimer.value = true
        }

        processViewModel.startTimer.observe(this,{
            if (it) {
                val bundle = Bundle()
                bundle.putString(AppConstants.CATEGORY_ID,intent.getStringExtra(AppConstants.CATEGORY_ID))

                val fragment = ImageProcessingStartedFragment()
                fragment.arguments = bundle

                // add select background fragment
                supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, fragment)
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