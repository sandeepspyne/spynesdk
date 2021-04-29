package com.spyneai.credits

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.spyneai.R
import com.spyneai.databinding.ActivityCreditPlansBinding

class CreditPlansActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreditPlansBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_credit_plans)

        binding.tvBuyNow.setOnClickListener {
            Toast.makeText(this,"sandeep singh",Toast.LENGTH_LONG).show()
        }
    }
}