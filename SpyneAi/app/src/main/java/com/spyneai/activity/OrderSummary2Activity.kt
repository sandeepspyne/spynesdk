package com.spyneai.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
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
import java.net.URLEncoder


class OrderSummary2Activity : AppCompatActivity() {

    private var TAG = "OrderSummary2Activity"

    private lateinit var listHdQuality : ArrayList<String>
    lateinit var productImage : String
    private lateinit var listWatermark : ArrayList<String>

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

        Utilities.savePrefrence(this, AppConstants.PRICE, Utilities.getPreference(this, AppConstants.NO_OF_IMAGES))


        tvTotalCost.setText(Utilities.getPreference(this, AppConstants.PRICE).toString())


        fetchUserCreditDetails()

        listHdQuality = ArrayList<String>()
        listWatermark = ArrayList<String>()

        listHdQuality.addAll(intent.getStringArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)

        listWatermark.addAll(intent.getStringArrayListExtra(AppConstants.LIST_WATERMARK)!!)

//        productImage = listHdQuality[0].toString()

        if (listHdQuality.size>0){
            Glide.with(this).load(
                listHdQuality[0].toString()
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
            if (Utilities.getPreference(this, AppConstants.CREDIT_AVAILABLE)!!.toInt() >= Utilities.getPreference(
                    this,
                    AppConstants.PRICE
                )!!.toInt()){
                val intent = Intent(this@OrderSummary2Activity, DownloadingActivity::class.java)
                Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "hd")
                intent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
                intent.putExtra(AppConstants.LIST_WATERMARK, listWatermark)
                startActivity(intent)
            }else{
                Toast.makeText(
                    this@OrderSummary2Activity,
                    "You are out of credits",
                    Toast.LENGTH_SHORT
                ).show()
                tvTopUp.startAnimation(AnimationUtils.loadAnimation(this, R.anim.move));
                val vibe = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibe.vibrate(100)
            }

        }

        tvTopUp.setOnClickListener {
            showWhatsappCreditDialog()
        }

        imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun fetchUserCreditDetails(){
        Log.d(TAG, "fetchUserCreditDetails: my id "+Utilities.getPreference(this, AppConstants.tokenId).toString())


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

    private fun showWhatsappCreditDialog(){

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.whatsapp_credit_dialog)
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        val ivClose: ImageView = dialog.findViewById(R.id.ivClose)
        val llRequestWappCredit: LinearLayout = dialog.findViewById(R.id.llRequestWappCredit)


        ivClose.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        llRequestWappCredit.setOnClickListener {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=" + "+919625429526" + "&text=" +
                        URLEncoder.encode(
                            "Spyne App is awesome!, I already used all my free credits. \n" +
                                    "Can you please help me get more credits.",
                            "UTF-8"
                        )
                i.setPackage("com.whatsapp")
                i.setData(Uri.parse(url))
                startActivity(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        dialog.show()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

