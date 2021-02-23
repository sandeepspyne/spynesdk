package com.spyneai.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.spyneai.R
import com.spyneai.needs.AppConstants
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.activity_preview.*

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        getPaymentData()
        listeners()
    }

    private fun getPaymentData() {
        if (intent.getStringExtra(AppConstants.AMOUNT) != null)
            tvAmount.setText(intent.getStringExtra(AppConstants.AMOUNT)!! + " /-")
        if (intent.getStringExtra(AppConstants.SKU_COUNT) != null)
            tvSkuCount.setText(intent.getStringExtra(AppConstants.SKU_COUNT)!!)
        if (intent.getStringExtra(AppConstants.CHANNEL_COUNT) != null)
            tvChannelCount.setText(intent.getStringExtra(AppConstants.CHANNEL_COUNT)!!)
    }

    private fun listeners() {
        ivBackPayment.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }
}