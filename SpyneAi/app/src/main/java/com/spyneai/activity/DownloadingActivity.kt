package com.spyneai.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.downloader.*
import com.spyneai.R
import com.spyneai.imagesdowloading.DownloadImageService
import com.spyneai.imagesdowloading.HDImagesDownloadedEvent
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.credit.UpdateCreditResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.videorecording.ThreeSixtyInteriorViewActivity
import com.spyneai.videorecording.model.ProcessVideoEvent
import kotlinx.android.synthetic.main.activity_downloading.*
import kotlinx.android.synthetic.main.activity_order_summary2.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DownloadingActivity : AppCompatActivity() {

    private lateinit var listWatermark: ArrayList<String>
    private lateinit var listHdQuality: ArrayList<String>
    var downloadCount: Int = 0
    var avaliableCredit: Int = 0
    var remaningCredit: Int = 0
    var price: Int = 0



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

//        if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("8")) {
//            Utilities.savePrefrence(this, AppConstants.PRICE, "5")
//        } else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("4")) {
//            Utilities.savePrefrence(this, AppConstants.PRICE, "3")
//        } else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("5")) {
//            Utilities.savePrefrence(this, AppConstants.PRICE, "5")
//        } else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("6")) {
//            Utilities.savePrefrence(this, AppConstants.PRICE, "5")
//        } else if (Utilities.getPreference(this, AppConstants.NO_OF_IMAGES).equals("7")) {
//            Utilities.savePrefrence(this, AppConstants.PRICE, "5")
//        }

        setPermissions()

        llButton.setOnClickListener {
            if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark")) {
                val intent = Intent(this, OrderSummary2Activity::class.java)
                intent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
                intent.putExtra(AppConstants.LIST_WATERMARK, listWatermark)
                startActivity(intent)
            } else {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            }
        }

        ivBack.setOnClickListener {
            onBackPressed()
        }


    }

    private fun setPermissions() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startImageDownload()
        } else {
            ActivityCompat.requestPermissions(
                this,
                DownloadingActivity.REQUIRED_PERMISSIONS,
                DownloadingActivity.REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = DownloadingActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun startImageDownload() {
        if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark")) {
            tvIncreaseSale.visibility = View.VISIBLE
            llButton.visibility = View.VISIBLE
            tvButtonText.setText("Download HD Images")
            downloadWatermark()
        } else if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("hd")) {
            ivBack.visibility = View.INVISIBLE
            llButton.visibility = View.VISIBLE
            tvIncreaseSale.visibility = View.INVISIBLE
            tvButtonText.setText("Go to Home")
            if (Utilities.getPreference(this, AppConstants.CREDIT_AVAILABLE)!!
                    .toInt() >= Utilities.getPreference(
                    this,
                    AppConstants.PRICE
                )!!.toInt()
            ) {
                avaliableCredit =
                    Utilities.getPreference(this, AppConstants.CREDIT_AVAILABLE)!!.toInt()
                price = Utilities.getPreference(this, AppConstants.PRICE)!!.toInt()
                remaningCredit = avaliableCredit - price

                //downloadHighQuality()

                //start service

                var imageDownloadingServiceIntent = Intent(this,DownloadImageService::class.java)
                imageDownloadingServiceIntent.action = "START"
                imageDownloadingServiceIntent.putExtra(AppConstants.LIST_HD_QUALITY,listHdQuality)
                imageDownloadingServiceIntent.putExtra(AppConstants.SKU_NAME,intent.getStringExtra(AppConstants.SKU_NAME))
                imageDownloadingServiceIntent.putExtra(AppConstants.SKU_ID,intent.getStringExtra(AppConstants.SKU_ID))
                imageDownloadingServiceIntent.putExtra(AppConstants.CREDIT_REMAINING,remaningCredit)
                imageDownloadingServiceIntent.putExtra(AppConstants.PRICE,price)
                ContextCompat.startForegroundService(this, imageDownloadingServiceIntent)

            } else {
                Toast.makeText(this, "You are out of credits", Toast.LENGTH_SHORT)
            }
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

    private fun downloadHighQuality() {
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

        val imageName: String = "Spyne" + SimpleDateFormat(
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
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {

                    var s = "";

                    if (downloadCount == listWatermark.size)
                        Toast.makeText(
                            this@DownloadingActivity,
                            "Download Completed", Toast.LENGTH_SHORT
                        ).show()

                    refreshGallery(file.getAbsolutePath(), this@DownloadingActivity)

                    tvDownloading.visibility = View.GONE
                    tvDownloadCompleted.visibility = View.VISIBLE
                    downloadCount = 0
                }

                override fun onError(error: com.downloader.Error?) {
                    if (downloadCount == listHdQuality.size) {
                        Toast.makeText(
                            this@DownloadingActivity,
                            "Download Failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    // The Folder location where all the files will be stored
    private val outputDirectory: String by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${Environment.DIRECTORY_DCIM}/Spyne-HD/"
        } else {
            "${getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path}/Spyne-HD/"
        }
    }

    //Download
    fun downloadWithHighQuality(imageFile: String?) {
        downloadCount++
        val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

//        showNotifications()

        val imageName: String = "Spyne" + SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".png"

        var file = File(Environment.getExternalStorageDirectory().toString() + "/Spyne")

        //File(outputDirectory).mkdirs()
        //val file = File("$outputDirectory/${System.currentTimeMillis()}.png")


        scanFile(file.getAbsolutePath())

        Log.d(TAG, "downloadWithHighQuality: "+Environment.getExternalStorageDirectory().toString() + "/Spyne/"+imageName)


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
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {

                    if (downloadCount == listHdQuality.size) {
                        Toast.makeText(
                            this@DownloadingActivity,
                            "Download Completed", Toast.LENGTH_SHORT
                        ).show()

                        refreshGallery(file.getAbsolutePath(), this@DownloadingActivity)

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
                    if (downloadCount == listHdQuality.size) {
                        Toast.makeText(
                            this@DownloadingActivity,
                            "Download Failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            })
    }

    private fun userUpdateCredit() {

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
        if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark")) {
            super.onBackPressed()
            finish()
        }
    }

    private fun scanFile(path: String) {
        Log.d(TAG, "scanFile: s"+path)
        MediaScannerConnection.scanFile(
            this@DownloadingActivity, arrayOf(path), null
        ) { path, uri -> Log.i("TAG", "Finished scanning $path") }
    }

    private fun refreshGallery(mCurrentPhotoPath: String, context: Context) {
        Log.d(TAG, "scanFile: r"+mCurrentPhotoPath)
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(mCurrentPhotoPath)
        val contentUri: Uri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: HDImagesDownloadedEvent?) {
        event?.getSkuId()?.let {
                                    Toast.makeText(
                            this@DownloadingActivity,
                            "Download Completed", Toast.LENGTH_SHORT
                        ).show()

                        tvDownloading.visibility = View.GONE
                        tvDownloadCompleted.visibility = View.VISIBLE
                        llButton.visibility = View.VISIBLE
                        tvButtonText.setText("Go to Home")
                       // userUpdateCredit()
                        downloadCount = 0
                        //llDownloadProgress.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DownloadingActivity.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startImageDownload()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}