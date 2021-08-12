package com.spyneai.activity

import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.spyneai.R
import com.spyneai.fragment.TopUpFragment

import com.spyneai.gotoHome
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_before_after.*
import kotlinx.android.synthetic.main.activity_downloading.*
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
    private lateinit var imageName: ArrayList<String>
    private var availableCredits = 0
    private var hdDownloaded = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary2)

        ivOrderSummaryHome.setOnClickListener {
            gotoHome()
        }


        getSkuIdDownloadStatus(intent.getBooleanExtra("is_paid",false))

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
        imageName = ArrayList<String>()

        listHdQuality.addAll(intent.getStringArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)
        listWatermark.addAll(intent.getStringArrayListExtra(AppConstants.LIST_WATERMARK)!!)
        imageName.addAll(intent.getStringArrayListExtra(AppConstants.LIST_IMAGE_NAME)!!)


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
                    TopUpFragment().show(supportFragmentManager,"TopUpFragment")
                }
            }
        }

        when(getString(R.string.app_name)){
            "Yalla Motors","Travo Photos",AppConstants.SWEEP -> tvTopUp.visibility = View.GONE

            else-> {
                tvTopUp.setOnClickListener {
                    TopUpFragment().show(supportFragmentManager,"TopUpFragment")
                }
            }
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
        val downloadIntent = Intent(this@OrderSummary2Activity, DownloadingActivity::class.java)
        Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "hd")
        downloadIntent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
        downloadIntent.putExtra(AppConstants.LIST_WATERMARK, listWatermark)
        downloadIntent.putExtra(AppConstants.LIST_IMAGE_NAME, imageName)
        downloadIntent.putExtra("is_paid",hdDownloaded)
        downloadIntent.putExtra(AppConstants.SKU_ID, Utilities.getPreference(this@OrderSummary2Activity, AppConstants.SKU_ID)
            .toString())
        downloadIntent.putExtra(AppConstants.SKU_NAME,intent.getStringExtra(AppConstants.SKU_NAME))
        downloadIntent.putExtra(AppConstants.IMAGE_TYPE,intent.getStringExtra(AppConstants.IMAGE_TYPE))
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
            text = Utilities.getPreference(this@OrderSummary2Activity, AppConstants.PRICE).toString()
        }
        tv_credits.apply {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            text = "Credits"
        }
    }

    private fun fetchUserCreditDetails(){
        val request = RetrofitClients.buildService(APiService::class.java)
        val call = request.userCreditsDetails(
            Utilities.getPreference(this, AppConstants.AUTH_KEY).toString()
        )

        call?.enqueue(object : Callback<CreditDetailsResponse> {
            override fun onResponse(
                call: Call<CreditDetailsResponse>,
                response: Response<CreditDetailsResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    tvCreditsAvailable.setText(response.body()?.data?.credit_available.toString()!!)
                    Utilities.savePrefrence(
                        this@OrderSummary2Activity,
                        AppConstants.CREDIT_ALLOTED,
                        response.body()?.data?.credit_allotted.toString()
                    )

                    availableCredits = response.body()?.data?.credit_available!!

                    Utilities.savePrefrence(
                        this@OrderSummary2Activity,
                        AppConstants.CREDIT_AVAILABLE,
                        response.body()?.data?.credit_available.toString()
                    )
                    Utilities.savePrefrence(
                        this@OrderSummary2Activity,
                        AppConstants.CREDIT_USED,
                        response.body()?.data?.credit_used.toString()
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

