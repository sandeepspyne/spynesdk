package com.spyneai.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.impl.model.Preference
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_before_after.*
import kotlinx.android.synthetic.main.activity_order_summary2.*
import kotlinx.android.synthetic.main.activity_order_summary2.tvCategoryName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class OrderSummary2Activity : AppCompatActivity() {

    private lateinit var listHdQuality : ArrayList<File>
    lateinit var productImage : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary2)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        fetchUserCreditDetails()

        listHdQuality = ArrayList<File>()

        listHdQuality.addAll(intent.getParcelableArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)

        productImage = listHdQuality[0].toString()

        Glide.with(this).load(
            productImage!!
        ).into(ivProductImage)

        tvCategoryName.setText(Utilities.getPreference(this, AppConstants.CATEGORY_NAME))
        tvNoOfImages.setText(Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).toString())
        tvSkuId.setText(Utilities.getPreference(this, AppConstants.SKU_ID).toString())
        tvTotalImagesClicked.setText(Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).toString())

        tvDownloadHdImages.setOnClickListener {
            val intent = Intent(this, DownloadingActivity::class.java)
            Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "hd")
            intent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
            startActivity(intent)
        }
    }

    private fun fetchUserCreditDetails(){

        val request = RetrofitClientSpyneAi.buildService(APiService::class.java)
        val call = request.userCreditsDetails(Utilities.getPreference(this, AppConstants.tokenId).toString())

        call?.enqueue(object : Callback<CreditDetailsResponse> {
            override fun onResponse(call: Call<CreditDetailsResponse>, response: Response<CreditDetailsResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful ) {
                    tvCreditsAvailable.setText(response.body()?.data?.creditAvailable.toString()!!)


                }
                else{
                    Toast.makeText(this@OrderSummary2Activity, "Server not responding!!!", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<CreditDetailsResponse>, t: Throwable) {
                Toast.makeText(this@OrderSummary2Activity, "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })

    }
}