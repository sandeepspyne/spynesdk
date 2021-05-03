package com.spyneai.credits

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.spyneai.R
import com.spyneai.databinding.ActivityLowCreditsBinding
import com.spyneai.videorecording.RecordVideoActivity


class LowCreditsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLowCreditsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_low_credits)

        binding.ivWallet.setOnClickListener {
            startActivity(Intent(this,WalletActivity::class.java))
        }

        binding.ivBack.setOnClickListener { onBackPressed() }

        binding.tvBuyCreditsNow.setOnClickListener {
            var intent = Intent(this,CreditPlansActivity::class.java)
            intent.putExtra("credit_available",intent.getIntExtra("credit_available",0))
            startActivity(intent)
        }

        //load gif
        Glide.with(this).asGif().load(R.raw.opps_gif)
            .into(binding.ivOppsGif)

    }
}