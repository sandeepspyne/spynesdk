package com.spyneai.sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.*
import com.spyneai.databinding.ActivitySignupSdkBinding

class SignupSDK : AppCompatActivity() {

    private lateinit var binding: ActivitySignupSdkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupSdkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, SignUpSdkFragment())
            .commit()

    }

    override fun onBackPressed() {

    }
}