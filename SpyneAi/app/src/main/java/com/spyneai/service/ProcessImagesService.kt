package com.spyneai.service

import UploadPhotoResponse
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.gson.Gson
import com.spyneai.activity.DashboardActivity
import com.spyneai.activity.ShowGifActivity
import com.spyneai.activity.ShowImagesActivity
import com.spyneai.adapter.MarketplacesAdapter
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.aipack.BulkUploadResponse
import com.spyneai.aipack.FetchBulkResponse
import com.spyneai.aipack.FetchGifRequest
import com.spyneai.aipack.FetchGifResponse
import com.spyneai.interfaces.*
import com.spyneai.model.ai.SendEmailRequest
import com.spyneai.model.ai.UploadGifResponse
import com.spyneai.model.ai.WaterMarkResponse
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.marketplace.FootwearBulkResponse
import com.spyneai.model.otp.OtpResponse
import com.spyneai.model.sku.Photos
import com.spyneai.model.sku.SkuResponse
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.model.skustatus.UpdateSkuStatusRequest
import com.spyneai.model.skustatus.UpdateSkuStatusResponse
import com.spyneai.model.upload.UploadResponse
import com.spyneai.model.uploadRough.UploadPhotoRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_timer.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class ProcessImagesService(val intent: Intent) : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    val progress = 1000
    var maxProgress = 120000
    var i = 0

    private lateinit var photsAdapter: PhotosAdapter
    private lateinit var photoList: List<Photos>
    private lateinit var photoListInteriors: List<Photos>

    lateinit var imageList: ArrayList<String>
    lateinit var imageListAfter: ArrayList<String>
    lateinit var interiorList: ArrayList<String>

    public lateinit var imageFileList: ArrayList<File>
    public lateinit var imageFileListFrames: ArrayList<Int>

    public lateinit var imageInteriorFileList: ArrayList<File>
    public lateinit var imageInteriorFileListFrames: ArrayList<Int>

    private var currentPOsition: Int = 0
    lateinit var carBackgroundList: ArrayList<CarBackgroundsResponse>
    lateinit var carbackgroundsAdapter: MarketplacesAdapter
    var backgroundSelect: String = ""
    var marketplaceId: String = ""
    var backgroundColour: String = ""

    var totalImagesToUPload: Int = 0
    var totalImagesToUPloadIndex: Int = 0
    lateinit var gifList: ArrayList<String>
    var gifLink: String = ""
    lateinit var image_url: ArrayList<String>
    var countGif: Int = 0
    lateinit var t: Thread
    var catName: String = ""

    override fun onBind(intent: Intent): IBinder? {
        log("Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            log("using an intent with action $action")
            when (action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> log("This should never happen. No action in the received intent")
            }
        } else {
            log(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log("The service has been created".toUpperCase())

        setIntents()

        val notification = createNotification()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("The service has been destroyed".toUpperCase())
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent =
            Intent(applicationContext, ProcessImagesService::class.java).also {
                it.setPackage(packageName)
            };
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        );
    }

    private fun startService() {
        if (isServiceStarted) return
        log("Starting the foreground service task")
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, com.spyneai.service.ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
//                    pingFakeServer()
                    uploadImageToBucket()
                }
                delay(1 * 60 * 1000)
            }
            log("End of the loop for the service")
        }
    }

    private fun stopService() {
        log("Stopping the foreground service")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, com.spyneai.service.ServiceState.STOPPED)
    }

    private fun pingFakeServer() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmZ")
        val gmtTime = df.format(Date())

        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val json =
            """
                {
                    "deviceId": "$deviceId",
                    "createdAt": "$gmtTime"
                }
            """
        try {
            Fuel.post("https://jsonplaceholder.typicode.com/posts")
                .jsonBody(json)
                .response { _, _, result ->
                    val (bytes, error) = result
                    if (bytes != null) {
                        log("[response bytes] ${String(bytes)}")
                    } else {
                        log("[response error] ${error?.message}")
                    }
                }
        } catch (e: Exception) {
            log("Error making the request: ${e.message}")
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "PROCESSING SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Process Images Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Process Images Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, DashboardActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else Notification.Builder(this)

        return builder
            .setContentTitle("Process Images Service")
            .setContentText("processing...")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    private fun setIntents() {

        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!

        if (intent.getStringExtra(AppConstants.MARKETPLACE_ID) != null)
            marketplaceId = intent.getStringExtra(AppConstants.MARKETPLACE_ID)!!

        if (intent.getStringExtra(AppConstants.BACKGROUND_COLOUR) != null)
            backgroundColour = intent.getStringExtra(AppConstants.BACKGROUND_COLOUR)!!

        backgroundSelect = intent.getStringExtra(AppConstants.BG_ID)!!

        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()
        image_url = ArrayList<String>()

        imageInteriorFileList = ArrayList<File>()
        imageInteriorFileListFrames = ArrayList<Int>()

        //Get Intents
        imageFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        imageFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)

        if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Automobiles")) {
            imageInteriorFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST)!!)
            imageInteriorFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_INTERIOR_FRAME_LIST)!!)
        }
        totalImagesToUPload = imageFileList.size
    }

    fun uploadImageToBucket() {
        if (Utilities.isNetworkAvailable(this)) {
            Log.e("Fisrt execution", "UploadImage Bucket")
            log("Start uploading images to bucket")
            //Get All Data to be uploaded

            val imageFile = setImageRaw(imageFileList[totalImagesToUPloadIndex])

            val request = RetrofitClients.buildService(APiService::class.java)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            val body = MultipartBody.Part.createFormData("image", imageFile!!.name, requestFile)
            val descriptionString = "false"

            val optimization = RequestBody.create(MultipartBody.FORM, descriptionString)
            val call = request.uploadPhoto(body, optimization)

            call?.enqueue(object : Callback<UploadResponse> {
                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    //
                    if (response.isSuccessful) {
                        log(
                            "" + totalImagesToUPloadIndex.toString() +
                                    " " + response.body()?.image.toString()
                        )
                        Log.e(
                            "uploadImageToBucket", totalImagesToUPloadIndex.toString() +
                                    " " + response.body()?.image.toString()
                        )

                        //  if (Utilities.getPreference(this@CameraActivity, AppConstants.MAIN_IMAGE).equals(""))
                        Utilities.savePrefrence(
                            this@ProcessImagesService,
                            AppConstants.MAIN_IMAGE,
                            response.body()?.image.toString()
                        )
                        uploadImageURLs()
                    } else {
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in uploading image to bucket",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    //
                    Log.e("Respo Image ", "Image error")
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {

            Toast.makeText(this@ProcessImagesService, "No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun uploadImageURLs() {
        if (Utilities.isNetworkAvailable(this)) {
            log("start upload image url")
            val request = RetrofitClient.buildService(APiService::class.java)
            val uploadPhotoName: String =
                Utilities.getPreference(this, AppConstants.MAIN_IMAGE)
                    .toString().split("/")[Utilities.getPreference(
                    this,
                    AppConstants.MAIN_IMAGE
                ).toString().split("/").size - 1]

            val uploadPhotoRequest = UploadPhotoRequest(
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!,
                "raw",
                imageFileListFrames[totalImagesToUPloadIndex],
                Utilities.getPreference(this, AppConstants.SHOOT_ID)!!,
                Utilities.getPreference(this, AppConstants.MAIN_IMAGE).toString(),
                uploadPhotoName,
                "EXTERIOR"
            )

            Log.e("Frame Number", intent.getIntExtra(AppConstants.FRAME, 1).toString())
            log("Frame Number: " + intent.getIntExtra(AppConstants.FRAME, 1).toString())
            val gson = Gson()
            //    val personString = gson.toJson(uploadPhotoRequest)
            //  val skuName = RequestBody.create(MediaType.parse("application/json"), personString)

            val call = request.uploadPhotoRough(
                Utilities.getPreference(this, AppConstants.tokenId), uploadPhotoRequest
            )

            call?.enqueue(object : Callback<UploadPhotoResponse> {
                override fun onResponse(
                    call: Call<UploadPhotoResponse>,
                    response: Response<UploadPhotoResponse>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if (totalImagesToUPloadIndex < totalImagesToUPload - 1) {
                                Log.e("uploadImageURLs", totalImagesToUPloadIndex.toString())

                                Log.e("IMage uploaded ", response.body()?.msgInfo.toString())
                                Log.e(
                                    "SKU ID", Utilities.getPreference(
                                        this@ProcessImagesService,
                                        AppConstants.SKU_ID
                                    ).toString()
                                )
                                //  totalImagesToUPloadIndex = totalImagesToUPloadIndex + 1
                                ++totalImagesToUPloadIndex
                                uploadImageToBucket()
                            } else {
                                totalImagesToUPloadIndex = 0
                                totalImagesToUPload = imageInteriorFileList.size

                                if (imageInteriorFileList != null && imageInteriorFileList.size > 0) {
                                    uploadImageToBucketInterior()
                                } else
                                    markSkuComplete()
                            }
                        } catch (e: Exception) {
                            Log.e("Except", e.printStackTrace().toString())
                        }

                    } else {
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in uploading image url. Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {

                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Respo Image ", "Image error")
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadImageToBucketInterior() {
        if (Utilities.isNetworkAvailable(this)) {

            Log.e("Fisrt execution", "UploadImage Bucket")
            log("start uploading interior to bucket")
            //Get All Data to be uploaded

            val imageFile = setImageRaw(imageInteriorFileList[totalImagesToUPloadIndex])

            val request = RetrofitClients.buildService(APiService::class.java)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            val body = MultipartBody.Part.createFormData("image", imageFile!!.name, requestFile)
            val descriptionString = "false"

            val optimization = RequestBody.create(MultipartBody.FORM, descriptionString)
            val call = request.uploadPhoto(body, optimization)

            call?.enqueue(object : Callback<UploadResponse> {
                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    //
                    if (response.isSuccessful) {
                        Log.e("Fisrt execution", "UploadImage Bucket")


                        Log.e("uploadImageToBucket", response.body()?.image.toString())
                        Log.e("uploadImageToBucket", totalImagesToUPloadIndex.toString())
                        log("uploadImageToBucketInterior" + response.body()?.image.toString())
                        log("uploadImageToBucketInterior" + response.body()?.image.toString())

                        //  if (Utilities.getPreference(this@CameraActivity, AppConstants.MAIN_IMAGE).equals(""))
                        Utilities.savePrefrence(
                            this@ProcessImagesService,
                            AppConstants.MAIN_IMAGE,
                            response.body()?.image.toString()
                        )
                        uploadImageURLsInterior()
                    } else {
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in uploading interior images.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    //
                    Log.e("Respo Image ", "Image error")
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadImageURLsInterior() {
        if (Utilities.isNetworkAvailable(this)) {
            log("start Uploading images url interior")
            val request = RetrofitClient.buildService(APiService::class.java)
            val uploadPhotoName: String =
                Utilities.getPreference(this, AppConstants.MAIN_IMAGE)
                    .toString().split("/")[Utilities.getPreference(
                    this,
                    AppConstants.MAIN_IMAGE
                ).toString().split("/").size - 1]

            val uploadPhotoRequest = UploadPhotoRequest(
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!,
                "raw",
                imageInteriorFileListFrames[totalImagesToUPloadIndex],
                Utilities.getPreference(this, AppConstants.SHOOT_ID)!!,
                Utilities.getPreference(this, AppConstants.MAIN_IMAGE).toString(),
                uploadPhotoName,
                "INTERIOR"
            )

            //    val personString = gson.toJson(uploadPhotoRequest)
            //  val skuName = RequestBody.create(MediaType.parse("application/json"), personString)

            val call = request.uploadPhotoRough(
                Utilities.getPreference(this, AppConstants.tokenId), uploadPhotoRequest
            )

            call?.enqueue(object : Callback<UploadPhotoResponse> {
                override fun onResponse(
                    call: Call<UploadPhotoResponse>,
                    response: Response<UploadPhotoResponse>
                ) {
                    if (response.isSuccessful) {
                        try {
                            if (totalImagesToUPloadIndex < totalImagesToUPload - 1) {
                                Log.e("uploadURLsInterior", totalImagesToUPloadIndex.toString())
                                log("uploadURLsInterior: " + totalImagesToUPloadIndex.toString())

                                Log.e("IMageInterioruploaded ", response.body()?.msgInfo.toString())
                                log("IMageInterioruploaded: " + response.body()?.msgInfo.toString())
                                Log.e(
                                    "Frame 1i",
                                    response.body()?.payload!!.data.currentFrame.toString()
                                )
                                log("Frame 1i: " + response.body()?.payload!!.data.currentFrame.toString())
                                Log.e(
                                    "Frame 2i",
                                    response.body()?.payload!!.data.totalFrames.toString()
                                )
                                log("Frame 2i: " + response.body()?.payload!!.data.totalFrames.toString())


                                ++totalImagesToUPloadIndex
                                uploadImageToBucketInterior()
                            } else {
                                markSkuComplete()
                            }
                        } catch (e: Exception) {
                            Log.e("Except", e.printStackTrace().toString())
                        }

                    } else {
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in uploading image url(INTERIOR)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {

                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Respo Image ", "Image error")
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    fun setImageRaw(photoFile: File): File? {
        val myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath())
        var ei: ExifInterface? = null
        try {
            ei = ExifInterface(photoFile.getAbsolutePath())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        assert(ei != null)
        val orientation = ei!!.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        val rotatedBitmap: Bitmap?
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(myBitmap!!, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(myBitmap!!, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(myBitmap!!, 270f)
            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = myBitmap
            else -> rotatedBitmap = myBitmap
        }

        return persistImage(rotatedBitmap!!)
    }

    public fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    public fun persistImage(bitmap: Bitmap): File? {
        var imageFile: File? = null
        if (applicationContext != null) {
            val filesDir: File = applicationContext.getFilesDir()
            imageFile = File(filesDir, "photo" + ".png")
            val os: OutputStream
            try {
                os = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, os)
                os.flush()
                os.close()
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error writing bitmap", e)
            }
        }
        return imageFile
    }

    //MArk the SKu as complete
    private fun markSkuComplete() {
        if (Utilities.isNetworkAvailable(this)) {
            log("start markSkuComplete")
            val request = RetrofitClient.buildService(APiService::class.java)

            val updateSkuStatusRequest = UpdateSkuStatusRequest(
                Utilities.getPreference(this, AppConstants.SKU_ID)!!,
                Utilities.getPreference(this, AppConstants.SHOOT_ID)!!,
                true
            )
            val call = request.updateSkuStauts(
                Utilities.getPreference(this, AppConstants.tokenId),
                updateSkuStatusRequest
            )

            call?.enqueue(object : Callback<UpdateSkuStatusResponse> {
                override fun onResponse(
                    call: Call<UpdateSkuStatusResponse>,
                    response: Response<UpdateSkuStatusResponse>
                ) {

                    if (response.isSuccessful) {
                        Log.e("Sku completed", "MArked Complete")
                        setSkuImages()
                    }
                }

                override fun onFailure(call: Call<UpdateSkuStatusResponse>, t: Throwable) {
                    setSkuImages()

                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSkuImages() {
        photoList = ArrayList<Photos>()
        photoListInteriors = ArrayList<Photos>()
        photsAdapter = PhotosAdapter(this, photoList,
            object : PhotosAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                }
            })

        fetchSkuData()
    }

    private fun fetchSkuData() {
        if (Utilities.isNetworkAvailable(this)) {
            log("start fetchSkuData")
            val request = RetrofitClient.buildService(APiService::class.java)

            val call = request.getSkuDetails(
                Utilities.getPreference(this, AppConstants.tokenId),
                Utilities.getPreference(this, AppConstants.SKU_ID).toString()
            )

            call?.enqueue(object : Callback<SkuResponse> {
                override fun onResponse(
                    call: Call<SkuResponse>,
                    response: Response<SkuResponse>
                ) {

                    if (response.isSuccessful) {
                        if (response.body()?.payload != null) {
                            if (response.body()?.payload!!.data.photos.size > 0) {
                                (photoList as ArrayList).clear()
                                (photoListInteriors as ArrayList).clear()

                                for (i in 0..response.body()?.payload!!.data.photos.size - 1) {
                                    if (response.body()?.payload!!.data.photos[i].photoType.equals("EXTERIOR"))
                                        (photoList as ArrayList).add(response.body()?.payload!!.data.photos[i])
                                    else
                                        (photoListInteriors as ArrayList).add(response.body()?.payload!!.data.photos[i])
                                }
                            }
                        }
                        photsAdapter.notifyDataSetChanged()

                        // Utilities.showProgressDialog(this@ProcessImagesService)
                        if (countGif < photoList.size) {
                            if (catName.equals("Automobiles")) {
                                bulkUpload(countGif)
                            } else if (catName.equals("Footwear")) {
                                bulkUploadFootwear(countGif)
                            }
                        }
                        // Utilities.showProgressDialog(this@ProcessImagesService)
                    } else {
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in fetch sku data", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<SkuResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    //Upload bulk data
    private fun bulkUpload(countsGif: Int) {
        if (Utilities.isNetworkAvailable(this)) {

            log("start bulk upload")
            val request = RetrofitClients.buildService(APiService::class.java)
            Log.e("Upload bulk", "started......")

            /*   val background : ArrayList<String> = ArrayList<String>()
           background.addAll(listOf(backgroundSelect))*/

//        image_url.add(photoList[countsGif].displayThumbnail)

            val background = RequestBody.create(
                MultipartBody.FORM,
                backgroundSelect
            )

            val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.tokenId)!!
            )
            val skuId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!
            )
            val imageUrl = RequestBody.create(
                MultipartBody.FORM,
                photoList[countsGif].displayThumbnail
            )
            Log.e(
                "Sku NAme Upload done",
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!
            )
            val skuName = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!
            )

            val windowStatus = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.WINDOWS)!!
            )

            val contrast = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.EXPOSURES)!!
            )

            val call =
                request.bulkUPload(
                    background, userId, skuId,
                    imageUrl, skuName, windowStatus, contrast
                )

            call?.enqueue(object : Callback<BulkUploadResponse> {
                override fun onResponse(
                    call: Call<BulkUploadResponse>,
                    response: Response<BulkUploadResponse>
                ) {
                    if (response.isSuccessful && response.body()!!.status == 200) {
                        ++countGif
                        if (countGif < photoList.size) {
                            Log.e("countGif", countGif.toString())
                            bulkUpload(countGif)
//                           (imageListWaterMark as ArrayList).add(response.body()!!.watermark_image)

                        } else if (photoListInteriors.size > 0) {
                            countGif = 0
                            if (countGif < photoListInteriors.size) {
                                addWatermark(countGif)
                            }
                        } else
                            fetchBulkUpload()

                        Log.e("Upload Replace", "bulk")
                        Log.e(
                            "Upload Replace SKU",
                            Utilities.getPreference(
                                this@ProcessImagesService,
                                AppConstants.SKU_NAME
                            )!!
                        )
                    } else {
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in bulk upload", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BulkUploadResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    //Fetch bulk data
    private fun fetchBulkUpload() {
        if (Utilities.isNetworkAvailable(this)) {

            Log.e("Fetchbulkupload", "started......")
            log("start featch bulk upload")

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
                    if (response.isSuccessful) {
                        Log.e("Upload Replace", "bulk Fetch")
                        imageList = ArrayList<String>()
                        imageListAfter = ArrayList<String>()
                        interiorList = ArrayList<String>()

                        imageList.clear()
                        imageListAfter.clear()
                        interiorList.clear()

                        for (i in 0..response.body()!!.size - 1) {
                            if (response.body()!![i].category.equals("Exterior")) {
                                imageList.add(response.body()!![i].input_image_url)
                                imageListAfter.add(response.body()!![i].output_image_url)
                            } else {
                                interiorList.add(response.body()!![i].output_image_url)
                            }
                        }
                        if (Utilities.getPreference(
                                this@ProcessImagesService,
                                AppConstants.CATEGORY_NAME
                            )
                                .equals("Automobiles")
                        ) {
                            fetchGif()
                        } else {
                            sendEmail()
                        }

                    } else {
                        fetchBulkUpload()
                    }
                }

                override fun onFailure(call: Call<List<FetchBulkResponse>>, t: Throwable) {
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()

                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bulkUploadFootwear(countsGif: Int) {
        if (Utilities.isNetworkAvailable(this)) {


            val request = RetrofitClients.buildService(APiService::class.java)
            Log.e("Upload bulk footwear", "started......")
            log("start bulk Upload Footwear")

            /*   val background : ArrayList<String> = ArrayList<String>()
           background.addAll(listOf(backgroundSelect))*/

//        image_url.add(photoList[countsGif].displayThumbnail)

//            val background = RequestBody.create(
//                MultipartBody.FORM,
//                backgroundSelect
//            )

            val marketplace_id = RequestBody.create(
                MultipartBody.FORM,
                marketplaceId
            )

            val bg_color = RequestBody.create(
                MultipartBody.FORM,
                backgroundColour
            )

            val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.tokenId)!!
            )
            val skuId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!
            )
            val imageUrl = RequestBody.create(
                MultipartBody.FORM,
                photoList[countsGif].displayThumbnail
            )
            Log.e(
                "Sku NAme Upload",
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!
            )
            val skuName = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!
            )

            val windowStatus = RequestBody.create(
                MultipartBody.FORM,
                "inner"
            )

            val call =
                request.bulkUPloadFootwear(
                    userId,
                    skuId,
                    imageUrl,
                    skuName,
                    marketplace_id,
                    bg_color
                )

            call?.enqueue(object : Callback<FootwearBulkResponse> {
                override fun onResponse(
                    call: Call<FootwearBulkResponse>,
                    response: Response<FootwearBulkResponse>
                ) {
                    if (response.isSuccessful && response.body()!!.status == 200) {
                        ++countGif
                        if (countGif < photoList.size) {
                            Log.e("countGif", countGif.toString())
                            bulkUploadFootwear(countGif)
                        } else
                            fetchBulkUpload()
                        Log.e("Upload Replace", "bulk")
                        Log.e(
                            "Upload Replace SKU",
                            Utilities.getPreference(
                                this@ProcessImagesService,
                                AppConstants.SKU_NAME
                            )!!
                        )
                    } else {
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in bulk upload footwear", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<FootwearBulkResponse>, t: Throwable) {


                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addWatermark(countsGif: Int) {
        if (Utilities.isNetworkAvailable(this)) {


            val request = RetrofitClients.buildService(APiService::class.java)
            Log.e("Watermark bulk", "started......")
            log("start add watermark")

            val background = RequestBody.create(
                MultipartBody.FORM,
                backgroundSelect
            )

            val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.tokenId)!!
            )
            val skuId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!
            )
            val imageUrl = RequestBody.create(
                MultipartBody.FORM,
                photoListInteriors[countsGif].displayThumbnail
            )

            val skuName = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!
            )

            val call =
                request.addWaterMark(
                    background, userId, skuId,
                    imageUrl, skuName
                )

            call?.enqueue(object : Callback<WaterMarkResponse> {
                override fun onResponse(
                    call: Call<WaterMarkResponse>,
                    response: Response<WaterMarkResponse>
                ) {
                    if (response.isSuccessful && response.body()!!.status == 200) {
                        ++countGif
                        if (countGif < photoListInteriors.size) {
                            Log.e("countGif", countGif.toString())
                            addWatermark(countGif)
                        } else
                            fetchBulkUpload()
                        Log.e("Upload Replace", "bulk")
                        Log.e(
                            "Upload Replace SKU",
                            Utilities.getPreference(
                                this@ProcessImagesService,
                                AppConstants.SKU_NAME
                            )!!
                        )
                    } else {
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in add watermark", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<WaterMarkResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchGif() {
        if (Utilities.isNetworkAvailable(this)) {
            log("start fetch gif")
            val request = RetrofitClientsBulk.buildService(APiService::class.java)
            val fetchGifRequest = FetchGifRequest(imageListAfter)

            val call = request.fetchGif(fetchGifRequest)

            call?.enqueue(object : Callback<FetchGifResponse> {
                override fun onResponse(
                    call: Call<FetchGifResponse>,
                    response: Response<FetchGifResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.e("Upload Replace", "bulk gif fetched")


                        gifLink = response.body()!!.url
                        uploadGif()
                    } else {
                        fetchBulkUpload()
                    }
                }

                override fun onFailure(call: Call<FetchGifResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadGif() {
        if (Utilities.isNetworkAvailable(this)) {
            log("start upload gif")
            val request = RetrofitClients.buildService(APiService::class.java)

            val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.tokenId)!!
            )

            val skuId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!
            )
            val gifUrl = RequestBody.create(
                MultipartBody.FORM,
                gifLink
            )

            val call = request.uploadUserGif(userId, skuId, gifUrl)

            call?.enqueue(object : Callback<UploadGifResponse> {
                override fun onResponse(
                    call: Call<UploadGifResponse>,
                    response: Response<UploadGifResponse>
                ) {
                    if (response.isSuccessful) {
                        sendEmail()
                    }
                }

                override fun onFailure(call: Call<UploadGifResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail() {
        if (Utilities.isNetworkAvailable(this)) {
            log("start send email")
            val request = RetrofitClientSpyneAi.buildService(APiService::class.java)

            val sendEmailRequest = SendEmailRequest(
                imageList, imageListAfter, interiorList, gifLink,
                Utilities.getPreference(this, AppConstants.EMAIL_ID).toString()
            )
            val call = request.sendEmailAll(sendEmailRequest)

            call?.enqueue(object : Callback<OtpResponse> {
                override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                    if (response.isSuccessful) {
                        if (response.body()!!.id.equals("200")) {
//                            val intent = Intent(
//                                this@ProcessImagesService,
//                                ShowImagesActivity::class.java
//                            )
//                            intent.putExtra(AppConstants.GIF, gifLink)
//                            intent.putExtra(AppConstants.CATEGORY_NAME, catName)
//                            startActivity(intent)
//                            finish()
                            Toast.makeText(
                                this@ProcessImagesService,
                                response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                    Log.e("ok", "no way")
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

}
