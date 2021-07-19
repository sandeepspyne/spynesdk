package com.spyneai.threesixty.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.spyneai.R
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityThreeSixtyIntroBinding
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class ThreeSixtyIntroActivity : AppCompatActivity() {

    lateinit var binding: ActivityThreeSixtyIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThreeSixtyIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val threeSixtyViewModel = ViewModelProvider(this, ViewModelFactory()).get(
            ThreeSixtyViewModel::class.java)

        threeSixtyViewModel.title.observe(this,{
            binding.tvTitle.text = it
        })
    }

}