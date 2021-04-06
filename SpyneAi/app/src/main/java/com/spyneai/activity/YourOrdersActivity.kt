package com.spyneai.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.spyneai.R
import com.spyneai.databinding.ActivityYourOrdersBinding

class YourOrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityYourOrdersBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_your_orders)
    }
}