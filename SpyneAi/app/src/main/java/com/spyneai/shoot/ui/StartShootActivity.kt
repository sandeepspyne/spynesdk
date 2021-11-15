package com.spyneai.shoot.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.spyneai.R
import com.spyneai.databinding.ActivityStartShootBinding
import com.spyneai.needs.AppConstants
import com.spyneai.setLocale
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.dialogs.RequiredPermissionDialog
import com.spyneai.threesixty.ui.ThreeSixtyIntroActivity

class StartShootActivity : AppCompatActivity(){

    lateinit var binding : ActivityStartShootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLocale()



        binding = ActivityStartShootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Automoser
        if(getString(R.string.app_name) == AppConstants.AUTO_FOTO){
            binding.ivAutomobile.visibility=View.GONE
            binding.ivStartShoot.setImageResource(R.drawable.start_shoot_image_autofoto)
            binding.ivExploreThreeSixty.setImageResource(R.drawable.explore_three_sixty_autofoto)
        }else{
            binding.ivAutomobile.visibility=View.VISIBLE
            binding.ivStartShoot.setImageResource(R.drawable.start_shoot_image)
            binding.ivExploreThreeSixty.setImageResource(R.drawable.explore_three_sixty)
        }


        binding.ivBack.setOnClickListener {
            onBackPressed()
        }




        binding.flShootNow.setOnClickListener{

                val intent = Intent(this, ShootActivity::class.java)

                intent.putExtra(
                    AppConstants.CATEGORY_NAME,
                    "Automobiles")

                intent.putExtra(
                    AppConstants.CATEGORY_ID,
                    "cat_d8R14zUNE")

                startActivity(intent)



            }

        binding.tvExplore.setOnClickListener {
            Intent(this,ThreeSixtyIntroActivity::class.java)
                .apply {
                    putExtra(AppConstants.CATEGORY_ID,"cat_d8R14zUNE")
                    putExtra(AppConstants.CATEGORY_NAME,"Automobiles")
                    startActivity(this)
                }
        }
    }
}