package com.spyneai.credits

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.databinding.ActivityCreditFailedBinding

class CreditPaymentFailedActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreditFailedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_credit_failed)

        //load gif
        Glide.with(this).asGif().load(R.raw.payment_failed)
            .into(binding.ivFail)

    }
}