package com.spyneai.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.spyneai.R
import com.spyneai.needs.AppConstants
import kotlinx.android.synthetic.main.activity_payment.*
import org.json.JSONObject


class PaymentActivity : AppCompatActivity(), PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

       // getPaymentData()
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

        tvPay.setOnClickListener(View.OnClickListener {
            payRazor()
        })
    }

    //PAy using razorpay
    private fun payRazor() {
        val co = Checkout()
        try {
            val options = JSONObject()

            //Params for payment
            options.put("name", "Spyne AI")
            options.put("description", "Spyne AI Payment")
            //You can omit the image option to fetch the image from dashboard
            options.put(
                "image",
                "https://spyne-website.s3.ap-south-1.amazonaws.com/static/website-themes/spyne/images/spyne-black-logo.png"
            )
            options.put("currency", "INR")
            val payment: String = "10"
            var total = payment.toDouble()
            total = total * 100
            options.put("amount", total)
            val preFill = JSONObject()
            preFill.put("email", "situn.nanda@gmail.com")
            preFill.put("contact", "8920677851")
            options.put("prefill", preFill)
            co.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(
                this, "Error in payment: "
                        + e.message, Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        Log.e("Payme", " payment successfull " + p0.toString());
        Toast.makeText(this, "Payment successfully done! " + p0, Toast.LENGTH_SHORT).show();
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Log.e(
            "Payme",
            "error code " + java.lang.String.valueOf(p0)
                    + " -- Payment failed "
                    + p1.toString()
        )
        try {
            Toast.makeText(this, "Payment error please try again", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
            Log.e("OnPaymentError", "Exception in onPaymentError", e)
        }
    }
}