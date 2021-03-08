package com.spyneai.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.ordersummary.OrderSummaryResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_order_summary.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class OrderSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)

        fetchOrderSummary()
        openWhatsApp()
    }


    private fun fetchOrderSummary() {
        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getOrderSummary(
                Utilities.getPreference(this, AppConstants.tokenId),
                Utilities.getPreference(this, AppConstants.SHOOT_ID),
                Utilities.getPreference(this, AppConstants.SKU_ID))

        call?.enqueue(object : Callback<OrderSummaryResponse> {
            override fun onResponse(call: Call<OrderSummaryResponse>,
                                    response: Response<OrderSummaryResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.payload.data != null) {
                        tvCategoryName.text =
                                Utilities.getPreference(this@OrderSummaryActivity,
                                        AppConstants.CATEGORY_NAME)

                        tvOrderId.text =
                                Utilities.getPreference(this@OrderSummaryActivity,
                                        AppConstants.SHOOT_ID)

                        tvSkuCount.text = response.body()!!.payload.data.skuCount.toString()
                        tvSelectedBackgrounds.text = response.body()!!.payload.data.backgroundCount.toString()
                        tvDealershipLogo.text = "0"

                    } else {
                        Toast.makeText(this@OrderSummaryActivity,
                                "Something went wrong !!!", Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onFailure(call: Call<OrderSummaryResponse>, t: Throwable) {
                Toast.makeText(this@OrderSummaryActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun openWhatsApp() {
        tvRequest.setOnClickListener(View.OnClickListener {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=" + "+918644864461" + "&text=" +
                        URLEncoder.encode("Hi, Want to connect with Spyne.", "UTF-8")
                i.setPackage("com.whatsapp")
                i.setData(Uri.parse(url))
                if (i.resolveActivity(packageManager) != null) {
                    startActivity(i)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        imgBackSummary.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
