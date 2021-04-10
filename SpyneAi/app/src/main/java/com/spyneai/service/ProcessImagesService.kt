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
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.activity.DashboardActivity
import com.spyneai.adapter.MarketplacesAdapter
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.aipack.BulkUploadResponse
import com.spyneai.aipack.FetchBulkResponse
import com.spyneai.aipack.FetchGifRequest
import com.spyneai.aipack.FetchGifResponse
import com.spyneai.extras.events.ProcessingImagesEvent
import com.spyneai.interfaces.*
import com.spyneai.model.ai.SendEmailRequest
import com.spyneai.model.ai.UploadGifResponse
import com.spyneai.model.ai.WaterMarkResponse
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.marketplace.FootwearBulkResponse
import com.spyneai.model.otp.OtpResponse
import com.spyneai.model.sku.Photos
import com.spyneai.model.sku.SkuResponse
import com.spyneai.model.skustatus.UpdateSkuStatusRequest
import com.spyneai.model.skustatus.UpdateSkuStatusResponse
import com.spyneai.model.upload.UploadResponse
import com.spyneai.model.uploadRough.UploadPhotoRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class ProcessImagesService() : Service() {

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

    var intent: Intent? = null
    var notificationContentText: String = "Image processing service started..."

    private var notificationID = (0..999999).random()

    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder

    var PROGRESS_MAX = 1
    var PROGRESS_CURRENT = 0

//    private var serviceLooper : Looper ? = null
//    private var serviceHandler : ServiceHandler ? = null

    override fun onBind(intent: Intent): IBinder? {
        log("Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand executed with startId: $startId")
        log("cat name" + intent?.getStringExtra(AppConstants.CATEGORY_NAME)!!)
        this.intent = intent
        setIntents()

        if (intent != null) {
            val action = intent!!.action
            log("using an intent with action $action")
            when (action) {
                Actions.START.name -> {

                    var processingImageEvent = ProcessingImagesEvent();
                    processingImageEvent.setNotificationID(notificationID);
                    EventBus.getDefault().post(processingImageEvent)

                    val notification = createNotification(notificationContentText)
                    startForeground(notificationID, notification)
                    startService()
                }
                Actions.STOP.name ->
                    stopService()
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
//        val notification = createNotification()
//        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("The service has been destroyed".toUpperCase())
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartintent =
            Intent(applicationContext, ProcessImagesService::class.java).also {
                it.setPackage(packageName)
            };
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartintent, PendingIntent.FLAG_ONE_SHOT);
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
        if (isServiceStarted)
            return
        log("Starting the foreground service task")
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, com.spyneai.service.ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ProcessService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
//                    pingFakeServer()
                    uploadImageToBucket()
                    PROGRESS_CURRENT = 0
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
            val notification = outputNotification("Image processing completed.")
            startForeground(notificationID, notification)
//            stopSelf()
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

    private fun createNotification(notificationContentText: String): Notification {
        val notificationChannelId = "PROCESSING SERVICE CHANNEL"
        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channel = NotificationChannel(
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


        builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else
                Notification.Builder(this)

        return builder
            .setContentTitle("Spyne")
            .setContentText(notificationContentText)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.app_logo)
            .setTicker("Ticker text")
            .setOnlyAlertOnce(true)
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()


    }

    private fun updateNotification(notificationContentText: String): Notification {
        return builder
            .setContentText(notificationContentText)
            .setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
            .build()
        notificationManager.notify(notificationID, builder.build())
    }

    private fun outputNotification(notificationContentText: String): Notification {

        val pendingIntent: PendingIntent =
            Intent(this, CompletedProjectsActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        return builder
            .setContentText(notificationContentText)
            .setProgress(0, 0, false)
            .addAction(R.drawable.check, getString(R.string.output),
                pendingIntent)
            .build()
        notificationManager.notify(notificationID, builder.build())
    }



    private fun setIntents() {

        if (intent?.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent?.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!

        if (intent?.getStringExtra(AppConstants.MARKETPLACE_ID) != null)
            marketplaceId = intent?.getStringExtra(AppConstants.MARKETPLACE_ID)!!

        if (intent?.getStringExtra(AppConstants.BACKGROUND_COLOUR) != null)
            backgroundColour = intent?.getStringExtra(AppConstants.BACKGROUND_COLOUR)!!

        backgroundSelect = intent?.getStringExtra(AppConstants.BG_ID)!!

        log("CATEGORY_NAME" + catName)

        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()
        image_url = ArrayList<String>()

        imageInteriorFileList = ArrayList<File>()
        imageInteriorFileListFrames = ArrayList<Int>()

        //Get Intents
        imageFileList.addAll(intent?.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        imageFileListFrames.addAll(intent?.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)

        if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Automobiles")) {
            imageInteriorFileList.addAll(intent?.getParcelableArrayListExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST)!!)
            imageInteriorFileListFrames.addAll(intent?.getIntegerArrayListExtra(AppConstants.ALL_INTERIOR_FRAME_LIST)!!)
        }
        totalImagesToUPload = imageFileList.size
    }

    fun uploadImageToBucket() {
        PROGRESS_MAX = imageFileList.size
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

                        PROGRESS_CURRENT++
                        val notification = updateNotification("Exterior image uploading started...")
                        startForeground(notificationID, notification)

                        //  if (Utilities.getPreference(this@CameraActivity, AppConstants.MAIN_IMAGE).equals(""))
                        Utilities.savePrefrence(
                            this@ProcessImagesService,
                            AppConstants.MAIN_IMAGE,
                            response.body()?.image.toString()
                        )
                        uploadImageURLs()
                    } else {
//                        uploadImageToBucket()
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in uploading image to bucket",
                            Toast.LENGTH_SHORT
                        ).show()
                        log("Error in uploading image to bucket")
                        log("Error Body: "+response.errorBody())
                        log("Response: "+response.body())
                    }
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    //
                    Log.e("Respo Image ", "Image error")
//                    uploadImageToBucket()
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding",
                        Toast.LENGTH_SHORT
                    ).show()
                    log("Server not responding(uploadImageToBucket)")
                    log("onFailure: " +t.localizedMessage)
                }
            })
        } else {

            Toast.makeText(this@ProcessImagesService, "No Internet Connection", Toast.LENGTH_SHORT)
                .show()
            log("No Internet Connection")
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

            Log.e("Frame Number", intent?.getIntExtra(AppConstants.FRAME, 1).toString())
            log("Frame Number: " + intent?.getIntExtra(AppConstants.FRAME, 1).toString())
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
                                    PROGRESS_CURRENT = 0
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
                        log("Error in uploading image url. Please try again")
                        log("Error: "+response.errorBody())
                    }
                }

                override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {

                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding",
                        Toast.LENGTH_SHORT
                    ).show()
                    log("Server not responding(uploadImageURLs)")
                    Log.e("Respo Image ", "Image error")
                    log("onFailure: "+t.localizedMessage)
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
        }
    }

    fun uploadImageToBucketInterior() {
        PROGRESS_MAX = imageInteriorFileList.size
        PROGRESS_CURRENT = 0
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

                        PROGRESS_CURRENT++

                        val notification = updateNotification("Interior image uploading started...")
                        startForeground(notificationID, notification)

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
                        log("Error in uploading interior images.")
                        log("Error: "+response.errorBody())
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
                    log("Server not responding(uploadImageToBucketInterior)")
                    log("onFailure: "+t.localizedMessage)
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
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
                        log("Error in uploading image url(INTERIOR)")
                        log("Error: "+response.errorBody())
                    }
                }

                override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {

                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding",
                        Toast.LENGTH_SHORT
                    ).show()
                    log("Server not responding(uploadImageURLsInterior)")
                    Log.e("Respo Image ", "Image error")
                    log("onFailure: "+t.localizedMessage)
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
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
        PROGRESS_CURRENT = 0
        PROGRESS_CURRENT = 0
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

                    val notification = updateNotification("Mark SKU started...")
                    startForeground(notificationID, notification)

                    if (response.isSuccessful) {
                        Log.e("Sku completed", "MArked Complete")
                        setSkuImages()
                    }
                }

                override fun onFailure(call: Call<UpdateSkuStatusResponse>, t: Throwable) {
                    setSkuImages()
                    log("Server not responding(markSkuComplete)")
                    log("onFailure: "+t.localizedMessage)

                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
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
                                PROGRESS_CURRENT = 0
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
                        log("Error in fetch sku data")
                        log("Error: "+response.errorBody())
                    }
                }

                override fun onFailure(call: Call<SkuResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                    log("Server not responding(fetchSkuData)")
                    log("onFailure: "+t.localizedMessage)
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
        }
    }

    //Upload bulk data
    private fun bulkUpload(countsGif: Int) {
        PROGRESS_MAX = photoList.size
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
                        PROGRESS_CURRENT++

                        val notification = updateNotification("AI for Image Processing started...")
                        startForeground(notificationID, notification)

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
                        if (countGif < photoList.size) {
                            Log.e("countGif", countGif.toString())
                            bulkUpload(countGif)
//                           (imageListWaterMark as ArrayList).add(response.body()!!.watermark_image)

                        } else if (photoListInteriors.size > 0) {
                            countGif = 0
                            if (countGif < photoListInteriors.size) {
                                addWatermark(countGif)
                            }
                        } else{

                        }
                            fetchBulkUpload()
                        Toast.makeText(
                            this@ProcessImagesService,
                            "Error in bulk upload", Toast.LENGTH_SHORT
                        ).show()
                        log("Error in bulk upload")
                        log("Error: "+response.errorBody())
                        log("Response: "+response.body())
                    }
                }

                override fun onFailure(call: Call<BulkUploadResponse>, t: Throwable) {
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
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                    log("Server not responding(bulkUpload)")
                    log("onFailure: "+t.localizedMessage)
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
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
                        PROGRESS_MAX = response.body()!!.size

                        val notification = updateNotification("Fetching your images...")
                        startForeground(notificationID, notification)

                        Log.e("Upload Replace", "bulk Fetch")
                        imageList = ArrayList<String>()
                        imageListAfter = ArrayList<String>()
                        interiorList = ArrayList<String>()

                        imageList.clear()
                        imageListAfter.clear()
                        interiorList.clear()

                        for (i in 0..response.body()!!.size - 1) {
                            PROGRESS_CURRENT++
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
                    log("Server not responding(fetchBulkUpload)")
                    log("onFailure: "+t.localizedMessage)

                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
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

                        val notification = updateNotification("AI for Image Processing started...")
                        startForeground(notificationID, notification)

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
                        log("Error in bulk upload footwear")
                        log("Error: "+response.errorBody())
                    }
                }

                override fun onFailure(call: Call<FootwearBulkResponse>, t: Throwable) {


                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                    log("Server not responding(bulkUploadFootwear)")
                    log("onFailure: "+t.localizedMessage)
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
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
                        log("Error in add watermark")
                        log("Error: "+response.errorBody())
                    }
                }

                override fun onFailure(call: Call<WaterMarkResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ProcessImagesService,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                    log("Server not responding(addWatermark)")
                    log("onFailure: "+t.localizedMessage)
                }
            })
        } else {

            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
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
                    log("Server not responding(fetchGif)")
                    log("onFailure: "+t.localizedMessage)
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
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
                    log("Server not responding(uploadGif)")
                    log("onFailure: "+t.localizedMessage)
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")
        }
    }

    private fun sendEmail() {
        PROGRESS_MAX = 1
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
                            Toast.makeText(
                                this@ProcessImagesService,
                                response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            log("" + response.body()!!.message)
                            val notification = updateNotification("Output email sent...")
                            startForeground(notificationID, notification)
                            stopService()

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
                    log("Server not responding(sendEmail)")
                    log("onFailure"+ t.localizedMessage)
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            log("No Internet Connection")

        }
    }

}
