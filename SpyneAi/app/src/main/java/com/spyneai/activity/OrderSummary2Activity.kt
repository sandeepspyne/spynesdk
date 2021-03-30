package com.spyneai.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.spyneai.R
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_before_after.*
import kotlinx.android.synthetic.main.activity_order_summary2.*
import kotlinx.android.synthetic.main.activity_order_summary2.tvCategoryName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class OrderSummary2Activity : AppCompatActivity() {

    private lateinit var listHdQuality : ArrayList<String>
    lateinit var productImage : String

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary2)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        PRDownloader.initialize(getApplicationContext());
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(applicationContext, config)

        if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("8")){
            Utilities.savePrefrence(this, AppConstants.PRICE, "5")
        }else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("4")){
            Utilities.savePrefrence(this, AppConstants.PRICE, "3")
        }else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("5")){
            Utilities.savePrefrence(this, AppConstants.PRICE, "5")
        }

        tvTotalCost.setText(Utilities.getPreference(this, AppConstants.PRICE).toString())



        fetchUserCreditDetails()

        listHdQuality = ArrayList<String>()

        listHdQuality.addAll(intent.getStringArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)

        productImage = listHdQuality[0].toString()

        Glide.with(this).load(
            productImage!!
        ).into(ivProductImage)

        tvCategoryName.setText(Utilities.getPreference(this, AppConstants.CATEGORY_NAME))
        tvNoOfImages.setText(Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).toString())
        tvSkuId.setText(Utilities.getPreference(this, AppConstants.SKU_ID).toString())
        tvTotalImagesClicked.setText(
            Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).toString()
        )

        tvDownloadHdImages.setOnClickListener {
            if (Utilities.getPreference(this, AppConstants.CREDIT_AVAILABLE).toString() >= Utilities.getPreference(
                    this,
                    AppConstants.PRICE
                ).toString()){
                val intent = Intent(this, DownloadingActivity::class.java)
                Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "hd")
                intent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
                startActivity(intent)
            }else{
                tvCreditAvailable.startAnimation(AnimationUtils.loadAnimation(this, R.anim.move));
                val vibe = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibe.vibrate(100)
                Toast.makeText(this, "You are out of credits", Toast.LENGTH_SHORT)
            }

        }
    }

    private fun fetchUserCreditDetails(){

        val request = RetrofitClientSpyneAi.buildService(APiService::class.java)
        val call = request.userCreditsDetails(
            Utilities.getPreference(this, AppConstants.tokenId).toString()
        )

        call?.enqueue(object : Callback<CreditDetailsResponse> {
            override fun onResponse(
                call: Call<CreditDetailsResponse>,
                response: Response<CreditDetailsResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    tvCreditsAvailable.setText(response.body()?.data?.creditAvailable.toString()!!)
                    Utilities.savePrefrence(
                        this@OrderSummary2Activity,
                        AppConstants.CREDIT_ALLOTED,
                        response.body()?.data?.creditAlloted.toString()
                    )
                    Utilities.savePrefrence(
                        this@OrderSummary2Activity,
                        AppConstants.CREDIT_AVAILABLE,
                        response.body()?.data?.creditAvailable.toString()
                    )
                    Utilities.savePrefrence(
                        this@OrderSummary2Activity,
                        AppConstants.CREDIT_USED,
                        response.body()?.data?.creditUsed.toString()
                    )


                } else {
                    Toast.makeText(
                        this@OrderSummary2Activity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<CreditDetailsResponse>, t: Throwable) {
                Toast.makeText(
                    this@OrderSummary2Activity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }
}