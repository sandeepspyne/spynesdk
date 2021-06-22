package com.spyneai.activity

import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.spyneai.R
import com.spyneai.credits.CreditApiService
import com.spyneai.credits.CreditPlansActivity
import com.spyneai.credits.LowCreditsActivity
import com.spyneai.credits.RetrofitCreditClient
import com.spyneai.credits.model.DownloadHDRes
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

    private var TAG = "OrderSummary2Activity"

    private lateinit var listHdQuality : ArrayList<String>
    lateinit var productImage : String
    private lateinit var listWatermark : ArrayList<String>
    private var availableCredits = 0
    private var hdDownloaded = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary2)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        getSkuIdDownloadStatus()

        PRDownloader.initialize(getApplicationContext())

        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(applicationContext, config)

        Utilities.savePrefrence(this, AppConstants.PRICE, Utilities.getPreference(this, AppConstants.NO_OF_IMAGES))

        tvTotalCost.setText(Utilities.getPreference(this, AppConstants.PRICE).toString())

        fetchUserCreditDetails()

        listHdQuality = ArrayList<String>()
        listWatermark = ArrayList<String>()

        listHdQuality.addAll(intent.getStringArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)

        listWatermark.addAll(intent.getStringArrayListExtra(AppConstants.LIST_WATERMARK)!!)

//        productImage = listHdQuality[0].toString()

        if (listHdQuality.size >0 && listHdQuality[0] != null){
            Glide.with(this).load(
                listHdQuality[0]
            ).into(ivProductImage)
        }

        tvCategoryName.setText(
            Utilities.getPreference(
                this@OrderSummary2Activity,
                AppConstants.CATEGORY_NAME
            )
        )

        tvNoOfImages.setText(
            Utilities.getPreference(
                this@OrderSummary2Activity,
                AppConstants.NO_OF_IMAGES
            ).toString()
        )

        tvSkuId.setText(
            Utilities.getPreference(this@OrderSummary2Activity, AppConstants.SKU_ID).toString()
        )

        tvTotalImagesClicked.setText(
            Utilities.getPreference(this@OrderSummary2Activity, AppConstants.NO_OF_IMAGES)
                .toString()
        )

        tvDownloadHdImages.setOnClickListener {

            if (hdDownloaded){
                startDownloadActivity()
            }else{
                if (Utilities.getPreference(this, AppConstants.CREDIT_AVAILABLE)!!.toInt() >= Utilities.getPreference(
                    this,
                    AppConstants.PRICE
                )!!.toInt()){
                    startDownloadActivity()
            }else{
                    var intent = Intent(this,LowCreditsActivity::class.java)
                    intent.putExtra("image", listHdQuality[0])
                    intent.putExtra("credit_available",availableCredits)
                    startActivity(intent)
                }
            }
        }

        tvTopUp.setOnClickListener {
            //showWhatsappCreditDialog()
            var intent = Intent(this,CreditPlansActivity::class.java)
            intent.putExtra("credit_available",availableCredits)
            startActivity(intent)
        }

        imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUserCreditDetails()
    }

    private fun startDownloadActivity() {
        val intent = Intent(this@OrderSummary2Activity, DownloadingActivity::class.java)
        Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "hd")
        intent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
        intent.putExtra(AppConstants.LIST_WATERMARK, listWatermark)
        intent.putExtra(AppConstants.SKU_ID, Utilities.getPreference(this@OrderSummary2Activity, AppConstants.SKU_ID)
            .toString())
        intent.putExtra(AppConstants.SKU_NAME,intent.getStringExtra(AppConstants.SKU_NAME))
        intent.putExtra(AppConstants.IS_DOWNLOADED_BEFORE,hdDownloaded)
        startActivity(intent)
    }

    private fun getSkuIdDownloadStatus() {
        var call = RetrofitCreditClient("https://www.clippr.ai/api/v4/").buildService(CreditApiService::class.java)
            .getHDDownloadStatus(Utilities.getPreference(this,AppConstants.TOKEN_ID).toString(),
                Utilities.getPreference(this@OrderSummary2Activity, AppConstants.SKU_ID)
                    .toString(),"TaD1VC1Ko")

        call?.enqueue(object : Callback<DownloadHDRes>{
            override fun onResponse(call: Call<DownloadHDRes>, response: Response<DownloadHDRes>) {
                if (response.isSuccessful){
                    if (response.body()?.staus.toString() == "200") {
                        hdDownloaded = true
                        applyDownloadedUI()
                    }
                }else{
                    Toast.makeText(this@OrderSummary2Activity,"Error from server",Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DownloadHDRes>, t: Throwable) {
                Toast.makeText(this@OrderSummary2Activity,"Error from server",Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun applyDownloadedUI() {
        tvTotalCost.setTextColor(ContextCompat.getColor(this,R.color.credit_deducted))

        tv_credits.setTextColor(ContextCompat.getColor(this,R.color.credit_deducted))

        tvTotalCost.apply {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            text = Utilities.getPreference(this@OrderSummary2Activity, AppConstants.PRICE).toString()
        }
        tv_credits.apply {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            text = "Credits"
        }
    }

    private fun fetchUserCreditDetails(){
        val request = RetrofitClientSpyneAi.buildService(APiService::class.java)
        val call = request.userCreditsDetails(
            Utilities.getPreference(this, AppConstants.TOKEN_ID).toString()
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

                    availableCredits = response.body()?.data?.creditAvailable!!

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


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

