package com.spyneai.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import com.downloader.*
import com.spyneai.R
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_downloading.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DownloadingActivity : AppCompatActivity() {

    private lateinit var listWatermark : ArrayList<File>
    private lateinit var listHdQuality : ArrayList<File>
    var downloadCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloading)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        listWatermark = ArrayList<File>()
        listHdQuality = ArrayList<File>()

        listWatermark.addAll(intent.getParcelableArrayListExtra(AppConstants.LIST_WATERMARK)!!)

        listHdQuality.addAll(intent.getParcelableArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)

        if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark")) {
            tvIncreaseSale.visibility = View.VISIBLE
            llButton.visibility = View.VISIBLE
            tvButtonText.setText("Download HD Images")
            downloadWatermark()
        }else {
            downloadHighQuality()
        }

        llButton.setOnClickListener {
            if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark")){
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this, OrderSummary2Activity::class.java)
                intent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
                startActivity(intent)
            }
        }






    }


    fun downloadWatermark() {
        if (listWatermark.size > 0 && listWatermark != null) {
            for (i in 0 until 3/*imageListWaterMark.size*/) {
//                seekbarDownload.setProgress(i)
//                tvProgress.setText(i.toString() + "/" + listHdQuality.size)
                if (listWatermark[i] != null)
                    downloadWithWatermark(listWatermark[i].toString())
            }
        }
    }

    private fun downloadHighQuality(){
        if (listHdQuality.size > 0 && listHdQuality != null) {
            for (i in 0 until 3/*imageListWaterMark.size*/) {
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
            FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".png"

        val downloadId = PRDownloader.download(
            imageFile,
            Environment.getExternalStorageDirectory().toString() + "/Spyne",
            imageName)
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

                }

                override fun onError(error: com.downloader.Error?) {
                    TODO("Not yet implemented")
                    Toast.makeText(
                        this@DownloadingActivity,
                        "Download Failed.", Toast.LENGTH_SHORT
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
            FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".png"

        val downloadId = PRDownloader.download(
            imageFile,
            Environment.getExternalStorageDirectory().toString() + "/Spyne",
            imageName)
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



}