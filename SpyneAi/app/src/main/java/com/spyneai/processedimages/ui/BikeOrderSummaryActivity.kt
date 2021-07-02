package com.spyneai.processedimages.ui

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.spyneai.R
import com.spyneai.activity.DownloadingActivity
import com.spyneai.credits.CreditPlansActivity
import com.spyneai.credits.LowCreditsActivity
import com.spyneai.gotoHome
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_bike_order_summary.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BikeOrderSummaryActivity : AppCompatActivity() {

    private var TAG = "BikeOrderSummaryActivity"

    private lateinit var listHdQuality : ArrayList<String>
    lateinit var productImage : String
    private lateinit var listWatermark : ArrayList<String>
    private var availableCredits = 0
    private var hdDownloaded = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bike_order_summary)

        listHdQuality = ArrayList<String>()
        listWatermark = ArrayList<String>()

        listHdQuality.addAll(intent.getStringArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)

        listWatermark.addAll(intent.getStringArrayListExtra(AppConstants.LIST_WATERMARK)!!)


        ivOrderSummaryHome.setOnClickListener {
            gotoHome()
        }

        getSkuIdDownloadStatus(intent.getBooleanExtra("is_paid",false))

        PRDownloader.initialize(getApplicationContext())

        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(applicationContext, config)

        Utilities.savePrefrence(this, AppConstants.PRICE, listHdQuality.size.toString())

        tvTotalCost.setText(Utilities.getPreference(this, AppConstants.PRICE).toString())

        fetchUserCreditDetails()


//        productImage = listHdQuality[0].toString()

        if (listHdQuality.size >0 && listHdQuality[0] != null){
            Glide.with(this).load(
                listHdQuality[0]
            ).into(ivProductImage)
        }

        tvCategoryName.setText(
            Utilities.getPreference(
                this@BikeOrderSummaryActivity,
                AppConstants.CATEGORY_NAME
            )
        )

        tvNoOfImages.text = listHdQuality.size.toString()

        tvSkuId.setText(
            Utilities.getPreference(this@BikeOrderSummaryActivity, AppConstants.SKU_ID).toString()
        )

        tvTotalImagesClicked.text = listHdQuality.size.toString()

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
                    var intent = Intent(this, LowCreditsActivity::class.java)
                    intent.putExtra("image", listHdQuality[0])
                    intent.putExtra("credit_available",availableCredits)
                    startActivity(intent)
                }
            }
        }

        tvTopUp.setOnClickListener {
            //showWhatsappCreditDialog()
            var intent = Intent(this, CreditPlansActivity::class.java)
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
        val downloadIntent = Intent(this@BikeOrderSummaryActivity, DownloadingActivity::class.java)
        Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "hd")
        downloadIntent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
        downloadIntent.putExtra(AppConstants.LIST_WATERMARK, listWatermark)
        downloadIntent.putExtra("is_paid",intent.getBooleanExtra("is_paid",false))
        downloadIntent.putExtra(
            AppConstants.SKU_ID, Utilities.getPreference(this@BikeOrderSummaryActivity, AppConstants.SKU_ID)
            .toString())
        downloadIntent.putExtra(AppConstants.SKU_NAME,intent.getStringExtra(AppConstants.SKU_NAME))
        downloadIntent.putExtra(AppConstants.IS_DOWNLOADED_BEFORE,hdDownloaded)
        startActivity(downloadIntent)
    }

    private fun getSkuIdDownloadStatus(isPaid : Boolean) {
        if (isPaid){
            hdDownloaded = true
            applyDownloadedUI()
        }
    }

    private fun applyDownloadedUI() {
        tvTotalCost.setTextColor(ContextCompat.getColor(this,R.color.credit_deducted))

        tv_credits.setTextColor(ContextCompat.getColor(this,R.color.credit_deducted))

        tvTotalCost.apply {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            text = listHdQuality.size.toString()
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
                        this@BikeOrderSummaryActivity,
                        AppConstants.CREDIT_ALLOTED,
                        response.body()?.data?.creditAlloted.toString()
                    )

                    availableCredits = response.body()?.data?.creditAvailable!!

                    Utilities.savePrefrence(
                        this@BikeOrderSummaryActivity,
                        AppConstants.CREDIT_AVAILABLE,
                        response.body()?.data?.creditAvailable.toString()
                    )
                    Utilities.savePrefrence(
                        this@BikeOrderSummaryActivity,
                        AppConstants.CREDIT_USED,
                        response.body()?.data?.creditUsed.toString()
                    )


                } else {
                    Toast.makeText(
                        this@BikeOrderSummaryActivity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<CreditDetailsResponse>, t: Throwable) {
                Toast.makeText(
                    this@BikeOrderSummaryActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}