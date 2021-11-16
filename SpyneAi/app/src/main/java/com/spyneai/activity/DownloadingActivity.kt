package com.spyneai.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.downloader.*
import com.spyneai.R
import com.spyneai.credits.fragments.DownloadCompletedFragment
import com.spyneai.credits.fragments.FeedbackSubmittedFragment
import com.spyneai.gotoHome
import com.spyneai.imagesdowloading.HDImagesDownloadedEvent
import com.spyneai.imagesdowloading.ImageDownloadingService
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_downloading.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class DownloadingActivity : AppCompatActivity() {

    private lateinit var listWatermark: ArrayList<String>
    private lateinit var listHdQuality: ArrayList<String>
    private lateinit var imageName: ArrayList<String>
    var downloadCount: Int = 0
    var avaliableCredit: Int = 0
    var remaningCredit: Int = 0
    var price: Int = 0
    var path_save_photos: String = ""
    lateinit var file: File
    var backpressEnabled = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloading)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark"))
            backpressEnabled = true

        listWatermark = ArrayList<String>()
        listHdQuality = ArrayList<String>()
        imageName = ArrayList<String>()

        listWatermark.addAll(intent.getStringArrayListExtra(AppConstants.LIST_WATERMARK)!!)
        listHdQuality.addAll(intent.getStringArrayListExtra(AppConstants.LIST_HD_QUALITY)!!)
        imageName.addAll(intent.getStringArrayListExtra(AppConstants.LIST_IMAGE_NAME)!!)

        setPermissions()
//
//        ivDownloadingHome.setOnClickListener {
//            gotoHome()
//        }

        llButton.setOnClickListener {
            if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("watermark")) {
                val orderIntent = Intent(this, OrderSummary2Activity::class.java)
                orderIntent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
                orderIntent.putExtra(AppConstants.LIST_WATERMARK, listWatermark)
                orderIntent.putExtra(AppConstants.LIST_IMAGE_NAME, imageName)
                orderIntent.putExtra("is_paid",intent.getBooleanExtra("is_paid",false))
                startActivity(orderIntent)
            } else {
                gotoHome()
            }
        }

        ivBack.setOnClickListener {
            onBackPressed()
        }

        iv_home.setOnClickListener { gotoHome() }
    }

    private fun setPermissions() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startImageDownload()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
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
            startDownloading(true,listWatermark,false)
        } else if (Utilities.getPreference(this, AppConstants.DOWNLOAD_TYPE).equals("hd")) {
            ivBack.visibility = View.INVISIBLE
            llButton.visibility = View.VISIBLE
            tvIncreaseSale.visibility = View.INVISIBLE
            tvButtonText.setText("Go to Home")

            if (intent.getBooleanExtra(AppConstants.IS_DOWNLOADED_BEFORE,false)){
                startDownloading(true,listHdQuality,true)
            }else{
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

                    if (listHdQuality[0] == null){
                        Toast.makeText(this, "HD images are null.", Toast.LENGTH_SHORT).show()
                        tvDownloadFailed.visibility = View.VISIBLE
                        tvDownloadCompleted.visibility = View.GONE
                        tvDownloading.visibility = View.GONE
                    }else{
                        //start service
                        startDownloading(false,listHdQuality,true)
                    }
                }else{
                    Toast.makeText(this,"Not enough credits available",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startDownloading(isDownloaded : Boolean,imageList : ArrayList<String>,isHd : Boolean) {
        var imageDownloadingServiceIntent = Intent(this,ImageDownloadingService::class.java)
        imageDownloadingServiceIntent.action = "START"
        imageDownloadingServiceIntent.putExtra(AppConstants.LIST_HD_QUALITY,imageList)
        imageDownloadingServiceIntent.putExtra(AppConstants.LIST_IMAGE_NAME,imageName)
        imageDownloadingServiceIntent.putExtra(AppConstants.SKU_NAME,intent.getStringExtra(AppConstants.SKU_NAME))
        imageDownloadingServiceIntent.putExtra(AppConstants.SKU_ID,intent.getStringExtra(AppConstants.SKU_ID))
        imageDownloadingServiceIntent.putExtra(AppConstants.IMAGE_TYPE,intent.getStringExtra(AppConstants.IMAGE_TYPE))
        imageDownloadingServiceIntent.putExtra(AppConstants.CREDIT_REMAINING,remaningCredit)
        imageDownloadingServiceIntent.putExtra(AppConstants.PRICE,price)
        imageDownloadingServiceIntent.putExtra(AppConstants.IS_DOWNLOADED_BEFORE,isDownloaded)
        imageDownloadingServiceIntent.putExtra(AppConstants.IS_HD,isHd)
        ContextCompat.startForegroundService(this, imageDownloadingServiceIntent)
    }

    override fun onBackPressed() {
        if (backpressEnabled)
            super.onBackPressed()
    }

    private fun scanFile(path: String) {
        MediaScannerConnection.scanFile(
            this@DownloadingActivity, arrayOf(path), null
        ) { path, uri -> Log.i("TAG", "Finished scanning $path") }
    }

    private fun refreshGallery(mCurrentPhotoPath: String, context: Context) {
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

//    val download_completed: String = getString(R.string.download_completed)

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: HDImagesDownloadedEvent?) {
        event?.getSkuId()?.let {
            Toast.makeText(this@DownloadingActivity,"Download Completed" , Toast.LENGTH_SHORT).show()

            //add download complete fragment
            var downloadCompletedFragment = DownloadCompletedFragment()
            var args = Bundle()
            args.putString("image",listHdQuality.get(0))
            downloadCompletedFragment.arguments = args

            supportFragmentManager.beginTransaction()
                .add(R.id.fl_container,downloadCompletedFragment)
                .commit()

            downloadCount = 0
        }
    }

    fun addFeedbackFragment() {
        iv_home.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .add(R.id.fl_container, FeedbackSubmittedFragment())
            .commit()
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