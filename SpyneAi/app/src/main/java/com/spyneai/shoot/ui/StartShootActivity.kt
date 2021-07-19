package com.spyneai.shoot.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.R
import com.spyneai.databinding.ActivityStartShootBinding
import com.spyneai.threesixty.ui.ThreeSixtyIntroActivity

class StartShootActivity : AppCompatActivity() {

    lateinit var binding : ActivityStartShootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartShootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvExplore.setOnClickListener {
            startActivity(Intent(this,ThreeSixtyIntroActivity::class.java))
        }
    }
}