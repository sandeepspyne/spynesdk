package com.spyneai.activity

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.downloader.*
import com.spyneai.R
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.credit.UpdateCreditResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.SingleMediaScanner
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_downloading.*
import kotlinx.android.synthetic.main.activity_order_summary2.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DownloadingActivity : AppCompatActivity() {

    private lateinit var listWatermark : ArrayList<String>
    private lateinit var listHdQuality : ArrayList<String>
    var downloadCount: Int = 0
    var avaliableCredit: Int = 0
    var remaningCredit: Int = 0
    var price : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloading)

        PRDownloader.initialize(getApplicationContext());
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(getApplicationContext(), config)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        listWatermark = ArrayList<String>()
        listHdQuality = ArrayList<String>()

        listWatermark.addAll(intent.getStringArrayListExtra(AppConstants.LIST_WATERMARK)!!)

        listHdQuality.addAll(intent.getStringArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)

      if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("8")){
          Utilities.savePrefrence(this, AppConstants.PRICE, "5")
      }else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("4")){
          Utilities.savePrefrence(this, AppConstants.PRICE, "3")
      }else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("5")){
          Utilities.savePrefrence(this, AppConstants.PRICE, "5")
      }else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("6")){
          Utilities.savePrefrence(this, AppConstants.PRICE, "5")
      }else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("7")){
          Utilities.savePrefrence(this, AppConstants.PRICE, "5")
      }

        if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark")) {
            tvIncreaseSale.visibility = View.VISIBLE
            llButton.visibility = View.VISIBLE
            tvButtonText.setText("Download HD Images")
            downloadWatermark()
        }else if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("hd")) {
            ivBack.visibility = View.INVISIBLE
            llButton.visibility = View.VISIBLE
            tvIncreaseSale.visibility = View.INVISIBLE
            tvButtonText.setText("Go to Home")
            if (Utilities.getPreference(this, AppConstants.CREDIT_AVAILABLE)!!.toInt() >= Utilities.getPreference(
                    this,
                    AppConstants.PRICE
                )!!.toInt()){
                avaliableCredit = Utilities.getPreference(this, AppConstants.CREDIT_AVAILABLE)!!.toInt()
                price = Utilities.getPreference(this, AppConstants.PRICE)!!.toInt()
                remaningCredit = avaliableCredit - price
                downloadHighQuality()
            }else{
                Toast.makeText(this, "You are out of credits", Toast.LENGTH_SHORT)
            }

        }

        llButton.setOnClickListener {
            if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark")){
                val intent = Intent(this, OrderSummary2Activity::class.java)
                intent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
                intent.putExtra(AppConstants.LIST_WATERMARK, listWatermark)
                startActivity(intent)
            }else{
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            }
        }

            ivBack.setOnClickListener {
                onBackPressed()
            }




    }


    fun downloadWatermark() {
        if (listWatermark.size > 0 && listWatermark != null) {
            for (i in 0 until listWatermark.size/*imageListWaterMark.size*/) {
//                seekbarDownload.setProgress(i)
//                tvProgress.setText(i.toString() + "/" + listHdQuality.size)
                if (listWatermark[i] != null)
                    downloadWithWatermark(listWatermark[i].toString())
            }
        }
    }

    private fun downloadHighQuality(){
        if (listHdQuality.size > 0 && listHdQuality != null) {
            for (i in 0 until listHdQuality.size/*imageListWaterMark.size*/) {
//                seekbarDownload.setProgress(i)
//                tvProgress.setText(i.toString() + "/" + listHdQuality.size)
                if (listHdQuality[i] != null)
                    downloadWithHighQuality(listHdQuality[i].toString())
            }
        }

    }

    fun downloadWithWatermark(imageFile: String?) {
        downloadCount++
        val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

//        seekbarDownload.visibility = View.VISIBLE

//        showNotifications()

        val imageName : String = "Spyne" + SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".png"

        var file = File(Environment.getExternalStorageDirectory().toString() + "/Spyne")

        scanFile(file.getAbsolutePath());

//        SingleMediaScanner(this, file)


        val downloadId = PRDownloader.download(
            imageFile,
            Environment.getExternalStorageDirectory().toString() + "/Spyne",
            imageName
        )
            .build()
            .setOnStartOrResumeListener {
            }
            .setOnPauseListener {

            }
            .setOnCancelListener(object : OnCancelListener {
                override fun onCancel() {}
            })
            .setOnProgressListener(object : OnProgressListener {
                override fun onProgress(progress: Progress) {
//                    builder.setContentTitle(imageName)
//                        .setContentText(
//                            ((100 - (progress.currentBytes%100)).toInt())
//                                .toString() + "/" + "100"+ "%")
//                        .setProgress(100, (100 - (progress.currentBytes%100)).toInt(),
//                            false);
//
//                    with(NotificationManagerCompat.from(this@DownloadingActivity)) {
//                        // notificationId is a unique int for each notification that you must define
//                        notify(1, builder.build())
//                    }
//
//                    Log.e("Progress HD", imageFile + " " +
//                            ((100 - (progress.currentBytes%100)).toInt())
//                                .toString() + "/" + "100"+ "%")
//                    tvProgress.setText(imageName)
//                    tvProgressvalue.setText(((100 - (progress.currentBytes%100)).toInt())
//                        .toString() + "/" + "100" + "%")
//                    llDownloadProgress.visibility = View.VISIBLE
//                    seekbarDownload.setProgress((100 - (progress.currentBytes%100)).toInt())
                }
            })
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {

                    if (downloadCount == listWatermark.size)
                        Toast.makeText(
                            this@DownloadingActivity,
                            "Download Completed", Toast.LENGTH_SHORT
                        ).show()
                    tvDownloading.visibility = View.GONE
                    tvDownloadCompleted.visibility = View.VISIBLE
                    downloadCount = 0

                }

                override fun onError(error: com.downloader.Error?) {
                    Toast.makeText(
                        this@DownloadingActivity,
                        error.toString(), Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    //Download
    fun downloadWithHighQuality(imageFile: String?) {
        downloadCount++
        val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

//        showNotifications()

        val imageName : String = "Spyne" + SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".png"

        var file = File(Environment.getExternalStorageDirectory().toString() + "/Spyne")

        scanFile(file.getAbsolutePath());

        val downloadId = PRDownloader.download(
            imageFile,
            Environment.getExternalStorageDirectory().toString() + "/Spyne",
            imageName
        )
            .build()
            .setOnStartOrResumeListener {
            }
            .setOnPauseListener {

            }
            .setOnCancelListener(object : OnCancelListener {
                override fun onCancel() {}
            })
            .setOnProgressListener(object : OnProgressListener {
                override fun onProgress(progress: Progress) {
                    //showNotifications(((progress.totalBytes / 100) * progress.currentBytes).toInt())

//                    builder.setContentTitle(imageName)
//                        .setContentText(
//                            ((100 - (progress.currentBytes%100)).toInt())
//                                .toString() + "/" + "100"+ "%")
//                        .setProgress(100, (100 - (progress.currentBytes%100)).toInt(),
//                            false);
//
//                    with(NotificationManagerCompat.from(this@ShowImagesActivity)) {
//                        // notificationId is a unique int for each notification that you must define
//                        notify(1, builder.build())
//                    }
//
//                    llDownloadProgress.visibility = View.VISIBLE
//
//                    tvProgress.setText(imageName)
//                    tvProgressvalue.setText(((100 - (progress.currentBytes%100)).toInt())
//                        .toString() + "/" + "100" + "%")
//
//                    seekbarDownload.setProgress((100 - (progress.currentBytes%100)).toInt())
//
//                    Log.e("Progress HD", imageFile + " " +
//                            ((100 - (progress.currentBytes%100)).toInt())
//                                .toString() + "/" + "100")
                }
            })
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    if (downloadCount == listHdQuality.size) {
                        Toast.makeText(
                            this@DownloadingActivity,
                            "Download Completed", Toast.LENGTH_SHORT
                        ).show()
                        tvDownloading.visibility = View.GONE
                        tvDownloadCompleted.visibility = View.VISIBLE
                        llButton.visibility = View.VISIBLE
                        tvButtonText.setText("Go to Home")
                        userUpdateCredit()
                        downloadCount = 0
//                        llDownloadProgress.visibility = View.GONE
                    }

//                    seekbarDownload.visibility = View.GONE
                }

                override fun onError(error: com.downloader.Error?) {
                    Toast.makeText(
                        this@DownloadingActivity,
                        "Download Failed", Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun userUpdateCredit(){


        val userId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.tokenId)!!
        )

        val creditAvailable = RequestBody.create(
            MultipartBody.FORM,
            remaningCredit.toString()
        )

        val creditUsed = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.PRICE)
        )


        val request = RetrofitClients.buildService(APiService::class.java)
        val call = request.userUpdateCredit(userId, creditAvailable, creditUsed)

        call?.enqueue(object : Callback<UpdateCreditResponse> {
            override fun onResponse(
                call: Call<UpdateCreditResponse>,
                response: Response<UpdateCreditResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {

                } else {
                    Toast.makeText(
                        this@DownloadingActivity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<UpdateCreditResponse>, t: Throwable) {
                Toast.makeText(
                    this@DownloadingActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onBackPressed() {
        if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark")){
            super.onBackPressed()
            finish()
        }
    }

    private fun scanFile(path: String) {
        MediaScannerConnection.scanFile(
            this@DownloadingActivity, arrayOf(path), null
        ) { path, uri -> Log.i("TAG", "Finished scanning $path") }
    }



}