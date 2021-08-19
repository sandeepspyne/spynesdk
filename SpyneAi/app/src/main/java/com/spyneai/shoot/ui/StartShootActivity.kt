package com.spyneai.shoot.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.R
import com.spyneai.databinding.ActivityStartShootBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.threesixty.ui.ThreeSixtyIntroActivity

class StartShootActivity : AppCompatActivity() {

    lateinit var binding : ActivityStartShootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartShootBinding.inflate(layoutInflater)
        setContentView(binding.root)

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