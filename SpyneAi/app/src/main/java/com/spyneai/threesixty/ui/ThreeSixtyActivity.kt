package com.spyneai.threesixty.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityThreeSixtyBinding
import com.spyneai.databinding.ActivityThreeSixtyViewBinding
import com.spyneai.shoot.data.ShootViewModel
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

        ThreeSixtyExteriorGifDialog().show(supportFragmentManager,"ThreeSixtyExteriorGifDialog")

        threeSixtyViewModel.isDemoClicked.observe(this,{
            if (it){
                supportFragmentManager.beginTransaction()
                    .add(binding.flContainer.id,RecordVideoFragment())
                    .commit()
            }
        })
    }
}