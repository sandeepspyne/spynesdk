package com.spyneai.shoot.ui.base

import CameraFragment
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.CategoryDetails
import com.spyneai.shoot.data.model.CreateProjectRes
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.ui.OverlaysFragment
import com.spyneai.shoot.ui.dialogs.ShootExitDialog
import com.spyneai.shoot.ui.ecomwithgrid.GridEcomFragment
import com.spyneai.shoot.ui.ecomwithgrid.ProjectDetailFragment
import com.spyneai.shoot.ui.ecomwithgrid.SkuDetailFragment
import com.spyneai.shoot.ui.ecomwithoverlays.OverlayEcomFragment
import com.spyneai.shoot.utils.log
import java.io.File

class ShootPortraitActivity : AppCompatActivity() {

    lateinit var cameraFragment: CameraFragment
    lateinit var overlaysFragment: OverlaysFragment
    lateinit var gridEcomFragment: GridEcomFragment
    lateinit var overlayEcomFragment: OverlayEcomFragment
    lateinit var skuDetailFragment: SkuDetailFragment
    lateinit var projectDetailFragment: ProjectDetailFragment
    lateinit var shootViewModel : ShootViewModel
    val TAG = "ShootPortraitActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shoot_portrait)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        shootViewModel = ViewModelProvider(this, ViewModelFactory()).get(ShootViewModel::class.java)
        shootViewModel.skuNumber.value = 1
        try {
            shootViewModel.skuNumber.value = intent.getIntExtra("skuNumber", 1)
        }catch (e: Exception){
        }

        if (intent.getBooleanExtra(AppConstants.FROM_DRAFTS,false))
            setUpDraftsData()

        val categoryDetails = CategoryDetails()

        categoryDetails.apply {
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)
            gifList =  intent.getStringExtra(AppConstants.GIF_LIST)
        }

        shootViewModel.categoryDetails.value = categoryDetails

        when(shootViewModel.categoryDetails.value?.categoryName) {
            "Footwear" -> shootViewModel.processSku = false
        }

        cameraFragment = CameraFragment()
        overlaysFragment = OverlaysFragment()
        gridEcomFragment = GridEcomFragment()
        skuDetailFragment = SkuDetailFragment()
        projectDetailFragment = ProjectDetailFragment()
        overlayEcomFragment = OverlayEcomFragment()

        if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Automobiles")){
            if(savedInstanceState == null) { // initial transaction should be wrapped like this
                supportFragmentManager.beginTransaction()
                    .add(R.id.flCamerFragment, cameraFragment)
                    .add(R.id.flCamerFragment, overlaysFragment)
                    .commitAllowingStateLoss()
            }
        }else if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("E-Commerce")){
            if(savedInstanceState == null) { // initial transaction should be wrapped like this
                supportFragmentManager.beginTransaction()
                    .add(R.id.flCamerFragment, cameraFragment)
                    .add(R.id.flCamerFragment, gridEcomFragment)
                    .commitAllowingStateLoss()
            }

            try {
                val intent = intent
                shootViewModel.projectId.value = intent.getStringExtra("project_id")
                val sku = Sku()
                sku?.projectId = shootViewModel.projectId.value
                shootViewModel.categoryDetails.value?.imageType = "Ecom"
                shootViewModel.sku.value = sku
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }else if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Footwear")){
            shootViewModel.processSku = false
            if(savedInstanceState == null) { // initial transaction should be wrapped like this
                supportFragmentManager.beginTransaction()
                    .add(R.id.flCamerFragment, cameraFragment)
                    .add(R.id.flCamerFragment, overlayEcomFragment)
                    .commitAllowingStateLoss()
            }
            try {
                val intent = intent
                shootViewModel.projectId.value = intent.getStringExtra("project_id")
                val sku = Sku()
                sku?.projectId = shootViewModel.projectId.value
                sku.skuName = intent.getStringExtra(AppConstants.SKU_NAME)
                sku.skuId = intent.getStringExtra(AppConstants.SKU_ID)
                sku.categoryName = shootViewModel.categoryDetails.value?.categoryName
                shootViewModel.categoryDetails.value?.imageType = ""
                shootViewModel.sku.value = sku
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }



        if (allPermissionsGranted()) {
            onPermissionGranted()
        } else {
            permissionRequest.launch(permissions.toTypedArray())
        }

        shootViewModel.stopShoot.observe(this,{
            if(it){
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                supportFragmentManager.beginTransaction()
                    .add(R.id.flCamerFragment, skuDetailFragment)
                    .commit()
            }
        })

        shootViewModel.showProjectDetail.observe(this,{
            if(it){
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                supportFragmentManager.beginTransaction().remove(skuDetailFragment).commit()
                supportFragmentManager.beginTransaction().remove(cameraFragment).commit()
                supportFragmentManager.beginTransaction().remove(gridEcomFragment).commit()
                supportFragmentManager.beginTransaction()
                    .add(R.id.flCamerFragment, projectDetailFragment)
                    .commit()
            }
        })

        shootViewModel.addMoreAngle.observe(this, {
            if (it)
            supportFragmentManager.beginTransaction().remove(skuDetailFragment).commit()
        })

        shootViewModel.selectBackground.observe(this, {
            if (it) {
                // start process activity
                val intent = Intent(this, ProcessActivity::class.java)

                intent.apply {
                    this.putExtra("sku_id", shootViewModel.sku.value?.skuId)
                    this.putExtra("project_id", shootViewModel.sku.value?.projectId)
                    this.putExtra("exterior_angles", shootViewModel.exterirorAngles.value)
                    startActivity(this)
                }
            }
        })
    }

    private fun setUpDraftsData() {
        shootViewModel.fromDrafts = true
        shootViewModel.showVin.value = true
        shootViewModel.isProjectCreated.value = true
        shootViewModel.projectId.value =  intent.getStringExtra(AppConstants.PROJECT_ID)!!

        shootViewModel._createProjectRes.value = Resource.Success(
            CreateProjectRes(
            "",
            intent.getStringExtra(AppConstants.PROJECT_ID)!!,
            200)
        )

        //set sku data
        val sku = Sku()
        sku.projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
        sku.skuName = intent.getStringExtra(AppConstants.SKU_NAME)
        sku.skuId = intent.getStringExtra(AppConstants.SKU_ID)
        sku.categoryName = shootViewModel.categoryDetails.value?.categoryName

        shootViewModel.sku.value = sku

        if (intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0) != 0){
            //sub category selected
            shootViewModel.subCatName.value = intent.getStringExtra(AppConstants.SUB_CAT_NAME)

            shootViewModel.subCategory.value = NewSubCatResponse.Data(
                1,
                "",
                "",
                "",
                1,
                1,
                intent.getStringExtra(AppConstants.CATEGORY_ID)!!,
                intent.getStringExtra(AppConstants.SUB_CAT_ID)!!,
                intent.getStringExtra(AppConstants.SUB_CAT_NAME)!!,
                ""
            )

            shootViewModel.isSubCategoryConfirmed.value = true

            if (intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0) == intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)){
                val list = shootViewModel.getImagesbySkuId(shootViewModel.sku.value?.skuId!!)

                shootViewModel.shootList.value = ArrayList()


                for(image in list){
                    shootViewModel.shootList.value!!.add(
                        ShootData(image.imagePath!!,
                            image.projectId!!,
                            image.skuId!!,
                        "",
                            Utilities.getPreference(this,AppConstants.AUTH_KEY).toString(),
                        0)
                    )
                }

                shootViewModel.stopShoot.value = true
            }

        }
    }

    /**
     * Check for the permissions
     */
    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            onPermissionGranted()
        } else {
            Toast.makeText(this, R.string.message_no_permissions, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    open fun onPermissionGranted() = Unit


    companion object {
        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

    override fun onBackPressed() {
        ShootExitDialog().show(supportFragmentManager,"ShootExitDialog")
    }
}