package com.spyneai.credits

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.databinding.ActivityLowCreditsBinding


class LowCreditsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLowCreditsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLowCreditsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivWallet.setOnClickListener {
            startActivity(Intent(this,WalletActivity::class.java))
        }

        binding.ivBack.setOnClickListener { onBackPressed() }

        binding.tvBuyCreditsNow.setOnClickListener {
            Log.d("LowCreditsActivity", "onCreate: "+intent.getIntExtra("credit_available",0))
            var creditIntent = Intent(this,CreditPlansActivity::class.java)
            creditIntent.putExtra("credit_available",intent.getIntExtra("credit_available",0))
            startActivity(creditIntent)
            finish()
        }

        if (intent.getStringExtra("image") != null){
            Glide.with(this)
                .load(intent.getStringExtra("image"))
                .into(binding.ivDonwlaodPreview)
        }

        //load gif
        Glide.with(this).asGif().load(R.raw.opps_gif)
            .into(binding.ivOppsGif)
    }
}