package com.spyneai.credits

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.databinding.ActivityCreditPlansBinding
import com.spyneai.databinding.ActivityCreditSuccessBinding

class CreditSuccessActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreditSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_credit_success)

        //load gif
        Glide.with(this).asGif().load(R.raw.credits_added)
            .into(binding.ivWalletGif)

    }
}