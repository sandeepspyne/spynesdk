package com.spyneai.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.downloader.*
import com.spyneai.R
import com.spyneai.adapter.ShowReplacedImagesAdapter
import com.spyneai.adapter.ShowReplacedImagesInteriorAdapter
import com.spyneai.aipack.FetchBulkResponse
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ScrollingLinearLayoutManager
import com.spyneai.needs.Utilities
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import kotlinx.android.synthetic.main.activity_before_after.*
import kotlinx.android.synthetic.main.activity_camera2.*
import kotlinx.android.synthetic.main.activity_show_gif.*
import kotlinx.android.synthetic.main.activity_show_images.*
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.view_custom.view.*
import kotlinx.android.synthetic.main.view_images.view.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ShowImagesActivity : AppCompatActivity() {
    lateinit var builder: NotificationCompat.Builder
    lateinit var imageList: List<String>
    lateinit var imageListAfter: List<String>
    lateinit var imageListInterior: List<String>
    lateinit var downloadList1: ArrayList<String>
    lateinit var downloadList2: ArrayList<String>
    lateinit var imageListWaterMark: ArrayList<String>
    lateinit var listHdQuality: ArrayList<String>
    var catName : String = ""

    private lateinit var showReplacedImagesAdapter: ShowReplacedImagesAdapter
    private lateinit var ShowReplacedImagesInteriorAdapter: ShowReplacedImagesInteriorAdapter

    var downloadCount: Int = 0
    lateinit var Category: String

    var downloadHighQualityCount: Int = 5


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_images)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        PRDownloader.initialize(getApplicationContext());
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(applicationContext, config)

        downloadList1 = ArrayList<String>()
        downloadList1.add("https://watermarkly.com/images/index/text-watermark-sample4.jpg")
        downloadList1.add("https://watermarkly.com/images/index/text-watermark-sample4.jpg")
        downloadList2 = ArrayList<String>()
        downloadList2.add("https://res-3.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_256,w_256,f_auto,q_auto:eco/khr5sfcxphjiw3wizhsb")
        downloadList2.add("https://res-3.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_256,w_256,f_auto,q_auto:eco/khr5sfcxphjiw3wizhsb")


        imageListWaterMark = ArrayList<String>()

//        if (Utilities.getPreference(this, AppConstants.highQualityCount).equals("0")) {
//            downloadHighQualityCount = 0
//            tvHighQualityCount.setText(downloadHighQualityCount.toString())
//        } else if (Utilities.getPreference(this, AppConstants.highQualityCount).equals("1")) {
//            downloadHighQualityCount = 1
//            tvHighQualityCount.setText(downloadHighQualityCount.toString())
//        } else if (Utilities.getPreference(this, AppConstants.highQualityCount).equals("2")) {
//            downloadHighQualityCount = 2
//            tvHighQualityCount.setText(downloadHighQualityCount.toString())
//        } else if (Utilities.getPreference(this, AppConstants.highQualityCount).equals("3")) {
//            downloadHighQualityCount = 3
//            tvHighQualityCount.setText(downloadHighQualityCount.toString())
//        } else if (Utilities.getPreference(this, AppConstants.highQualityCount).equals("4")) {
//            downloadHighQualityCount = 4
//            tvHighQualityCount.setText(downloadHighQualityCount.toString())
//        } else if (Utilities.getPreference(this, AppConstants.highQualityCount).equals("5")) {
//            downloadHighQualityCount = 5
//            tvHighQualityCount.setText(downloadHighQualityCount.toString())
//        }

        setBulkImages()

        setListeners()

        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!

        if (catName.equals("Footwear")){
            tvViewGif.visibility = View.GONE
        }
    }

    private fun hideData(i: Int) {

        if (i == 0) {
            tvYourEmailIdReplaced.visibility = View.VISIBLE
            tvViewGif.visibility = View.VISIBLE
            tvInterior.visibility = View.VISIBLE
//            llDownloads.visibility = View.VISIBLE
        }
        else{
            tvYourEmailIdReplaced.visibility = View.GONE
            tvViewGif.visibility = View.GONE
            tvInterior.visibility = View.GONE
//            llDownloads.visibility = View.GONE
        }
    }

    private fun setListeners() {
        tvViewGif.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this,
                ShowGifActivity::class.java
            )
            startActivity(intent)
        })

        ivBackShowImages.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        ivHomeShowImages.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SKU_ID, "")
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()

            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
        })

        tvDownloadFree.setOnClickListener {
            Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "watermark")
            val intent = Intent(this, DownloadingActivity::class.java)
            intent.putExtra(AppConstants.LIST_WATERMARK, downloadList1)
            intent.putExtra(AppConstants.LIST_HD_QUALITY, downloadList2)
            startActivity(intent)
        }

        llDownloadHdImages.setOnClickListener {

            Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "hd")
            val intent = Intent(this, OrderSummary2Activity::class.java)
            intent.putExtra(AppConstants.LIST_HD_QUALITY, downloadList2)
            startActivity(intent)
        }

      /*  tvRequestWappImages.setOnClickListener(View.OnClickListener {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=" + "+919953325165" + "&text=" +
                        URLEncoder.encode(
                            "Hey! The Spyne 360° Shot looks impressive; I liked the user experience and would like to learn more about the commercial application and how I can best access this technology. I look forward to connecting!",
                            "UTF-8"
                        )
                i.setPackage("com.whatsapp")
                i.setData(Uri.parse(url))
                startActivity(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        llDownloadHighQuality.setOnClickListener(View.OnClickListener {
            if (Utilities.getPreference(this, AppConstants.highQualityCount).equals("0"))
                Toast.makeText(
                    this@ShowImagesActivity,
                    "you have reached your high-quality download limit!! Please contact us on WhatsApp for more credits.",
                    Toast.LENGTH_LONG
                ).show()
            else {
                /* llDownloadHighQuality.isEnabled = false
                 llDownloadHighQuality.isFocusable = false*/
                downloadHighQuality()
            }
        })

        llDownloadWithWatermark.setOnClickListener(View.OnClickListener {
            downloadWatermark()
            llDownloadWithWatermark.isEnabled = false
            llDownloadWithWatermark.isFocusable = false
        })*/
    }


    private fun setBulkImages() {
        imageList = ArrayList<String>()
        imageListAfter = ArrayList<String>()
        imageListWaterMark = ArrayList<String>()
        imageListInterior = ArrayList<String>()
        listHdQuality = ArrayList<String>()

        showReplacedImagesAdapter = ShowReplacedImagesAdapter(this,
            imageList as ArrayList<String>,
            imageListAfter as ArrayList<String>,
            object : ShowReplacedImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })

        ShowReplacedImagesInteriorAdapter = ShowReplacedImagesInteriorAdapter(this,
            imageListInterior as ArrayList<String>,
            object : ShowReplacedImagesInteriorAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    //   showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })

        rvImagesBackgroundRemoved.setLayoutManager(
            ScrollingLinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )

        rvInteriors.setLayoutManager(
            GridLayoutManager(
                this,
                2
            )
        )

        rvImagesBackgroundRemoved.setAdapter(showReplacedImagesAdapter)
        rvInteriors.setAdapter(ShowReplacedImagesInteriorAdapter)
        fetchBulkUpload()
    }

    //Fetch bulk data
    private fun fetchBulkUpload() {
        Utilities.showProgressDialog(this)
        val request = RetrofitClients.buildService(APiService::class.java)
        val userId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.tokenId)!!
        )
        val skuId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.SKU_ID)!!
        )

        val call = request.fetchBulkImage(userId, skuId)

        call?.enqueue(object : Callback<List<FetchBulkResponse>> {
            override fun onResponse(
                call: Call<List<FetchBulkResponse>>,
                response: Response<List<FetchBulkResponse>>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    for (i in 0..response.body()!!.size - 1) {
                        if (response.body()!![i].category.equals("Exterior")) {
                            Category = response.body()!![i].category
                            (imageList as ArrayList).add(response.body()!![i].input_image_url)
                            (imageListAfter as ArrayList).add(response.body()!![i].output_image_url)
                            (imageListWaterMark as ArrayList).add(response.body()!![i].watermark_image)
                            (listHdQuality as ArrayList).add(response.body()!![i].output_image_url)
                            hideData(0)
                        } else if (response.body()!![i].category.equals("Interior")) {
                            Category = response.body()!![i].category
                            (imageListInterior as ArrayList).add(response.body()!![i].output_image_url)
                            (imageListWaterMark as ArrayList).add(response.body()!![i].output_image_url)
                            (listHdQuality as ArrayList).add(response.body()!![i].input_image_url)
                            hideData(0)
                        } else {
                            Category = response.body()!![i].category
                            (imageList as ArrayList).add(response.body()!![i].input_image_url)
                            (imageListAfter as ArrayList).add(response.body()!![i].output_image_url)
                            hideData(1)
                        }

                    }
                }
                showReplacedImagesAdapter.notifyDataSetChanged()
                ShowReplacedImagesInteriorAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<List<FetchBulkResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(
                    this@ShowImagesActivity,
                    "Server not responding!!!", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SHOOT_ID, "")
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.CATEGORY_ID, "")
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.PRODUCT_ID, "")
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SKU_NAME, "")
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SKU_ID, "")
        val intent = Intent(this, DashboardActivity::class.java)

        val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
        updateSkuResponseList.clear()

        Utilities.setList(
            this@ShowImagesActivity,
            AppConstants.FRAME_LIST, updateSkuResponseList
        )
        startActivity(intent)
        finish()
    }

    fun showImagesDialog(position: Int) {
        Utilities.showProgressDialog(this)
        Handler().postDelayed({
            Utilities.hideProgressDialog()
        }, 3000)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_show_images)

        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val carouselViewImages: CarouselView = dialog.findViewById(R.id.carouselViewImages)
        val ivCrossImages: ImageView = dialog.findViewById(R.id.ivCrossImages)

        ivCrossImages.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        carouselViewImages.setPageCount(imageList.size);
        carouselViewImages.setViewListener(viewListener);
        carouselViewImages.setCurrentItem(position)

        dialog.show()
    }

    var viewListener = object : ViewListener {
        override fun setViewForPosition(position: Int): View? {
            val customView: View = layoutInflater.inflate(R.layout.view_images, null)

            Glide.with(this@ShowImagesActivity) // replace with 'this' if it's in activity
                .load(imageList[position])
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(customView.ivBefore)
            Glide.with(this@ShowImagesActivity) // replace with 'this' if it's in activity
                .load(imageListAfter[position])
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(customView.ivAfter)

            return customView
        }
    }

    /*  fun downloadHighQuality() {
         Toast.makeText(
             this@ShowImagesActivity,
             "Download started", Toast.LENGTH_SHORT
         ).show()

         if (listHdQuality.size > 0 && listHdQuality != null) {
             for (i in 0 until listHdQuality.size) {
                 seekbarDownload.setProgress(i)

                 if (listHdQuality[i] != null)
                     downloadWithHighQuality(listHdQuality[i].toString())
             }
         }
     }

     fun downloadWatermark() {
         if (imageListWaterMark.size > 0 && imageListWaterMark != null) {
             for (i in 0 until 3/*imageListWaterMark.size*/) {
                 seekbarDownload.setProgress(i)
                 tvProgress.setText(i.toString() + "/" + listHdQuality.size)
                 if (imageListWaterMark[i] != null)
                     downloadWithWatermark(imageListWaterMark[i].toString())
             }
         }
     }

     //Download
    fun downloadWithHighQuality(imageFile: String?) {
         downloadCount++

         val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

         showNotifications()

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

                     builder.setContentTitle(imageName)
                         .setContentText(
                             ((100 - (progress.currentBytes%100)).toInt())
                                 .toString() + "/" + "100"+ "%")
                         .setProgress(100, (100 - (progress.currentBytes%100)).toInt(),
                             false);

                     with(NotificationManagerCompat.from(this@ShowImagesActivity)) {
                         // notificationId is a unique int for each notification that you must define
                         notify(1, builder.build())
                     }

                     llDownloadProgress.visibility = View.VISIBLE

                     tvProgress.setText(imageName)
                     tvProgressvalue.setText(((100 - (progress.currentBytes%100)).toInt())
                         .toString() + "/" + "100" + "%")

                     seekbarDownload.setProgress((100 - (progress.currentBytes%100)).toInt())

                     Log.e("Progress HD", imageFile + " " +
                             ((100 - (progress.currentBytes%100)).toInt())
                                 .toString() + "/" + "100")
                 }
             })
             .start(object : OnDownloadListener {
                 override fun onDownloadComplete() {
                     if (downloadCount == listHdQuality.size) {
                         Toast.makeText(
                             this@ShowImagesActivity,
                             "Download Completed", Toast.LENGTH_SHORT
                         ).show()

                         downloadHighQualityCount--
                         tvHighQualityCount.setText(downloadHighQualityCount.toString())
                         Utilities.savePrefrence(
                             this@ShowImagesActivity,
                             AppConstants.highQualityCount,
                             downloadHighQualityCount.toString()
                         )
                         downloadCount = 0
                         llDownloadProgress.visibility = View.GONE
                         llDownloadHighQuality.isEnabled = true
                         llDownloadHighQuality.isFocusable = true
                     }

                     seekbarDownload.visibility = View.GONE
                 }

                 override fun onError(error: com.downloader.Error?) {
                     Toast.makeText(
                         this@ShowImagesActivity,
                         "Download Failed", Toast.LENGTH_SHORT
                     ).show()
                 }

             })
     }

     fun downloadWithWatermark(imageFile: String?) {
         downloadCount++
         val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

         seekbarDownload.visibility = View.VISIBLE

         showNotifications()

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
                     builder.setContentTitle(imageName)
                         .setContentText(
                             ((100 - (progress.currentBytes%100)).toInt())
                                 .toString() + "/" + "100"+ "%")
                         .setProgress(100, (100 - (progress.currentBytes%100)).toInt(),
                             false);

                     with(NotificationManagerCompat.from(this@ShowImagesActivity)) {
                         // notificationId is a unique int for each notification that you must define
                         notify(1, builder.build())
                     }

                     Log.e("Progress HD", imageFile + " " +
                             ((100 - (progress.currentBytes%100)).toInt())
                                 .toString() + "/" + "100"+ "%")
                     tvProgress.setText(imageName)
                     tvProgressvalue.setText(((100 - (progress.currentBytes%100)).toInt())
                         .toString() + "/" + "100" + "%")
                     llDownloadProgress.visibility = View.VISIBLE
                     seekbarDownload.setProgress((100 - (progress.currentBytes%100)).toInt())
                 }
             })
             .start(object : OnDownloadListener {
                 override fun onDownloadComplete() {

                     if (downloadCount == imageListWaterMark.size)
                         Toast.makeText(
                             this@ShowImagesActivity,
                             "Download Completed", Toast.LENGTH_SHORT
                         ).show()

                     llDownloadWithWatermark.isEnabled = true
                     llDownloadWithWatermark.isFocusable = true
                     llDownloadProgress.visibility = View.GONE
                 }

                 override fun onError(error: com.downloader.Error?) {
                     TODO("Not yet implemented")
                     Toast.makeText(
                         this@ShowImagesActivity,
                         "Download Failed.", Toast.LENGTH_SHORT
                     ).show()
                 }
             })
     }

    */


    private fun getOutputDirectory(): String? {
        val mediaDir =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                externalMediaDirs.firstOrNull()?.let {
                    File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
                }
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir.toString() + File.separator
        else
            filesDir.toString() + File.separator
    }

    fun showNotifications()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val intent = Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            builder = NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.mipmap.home)
                .setContentTitle("MAin")
                .setContentText("Sub")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            builder.setProgress(100, 0, false);

            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(1, builder.build())
            }

        }
    }

/*
    private fun processProgress(s: String)
    {
        var cdt: CountDownTimer
        val id: Int = 1
        val mNotifyManager: NotificationManager
        val mBuilder: NotificationCompat.Builder
        try {
            mNotifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mBuilder = NotificationCompat.Builder(this)
            mBuilder.setContentTitle("Downloading File")
                .setContentText(s)
                .setProgress(0, 100, false)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.app_icon)
                .setPriority(Notification.PRIORITY_HIGH)


            mBuilder.setContentInfo(values[0] + "%")
                .setProgress(100, Integer.parseInt(values[0]), false);

            // Initialize Objects here
            mNotifyManager.notify(id, mBuilder.build())

            // Create connection here
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
*/


}