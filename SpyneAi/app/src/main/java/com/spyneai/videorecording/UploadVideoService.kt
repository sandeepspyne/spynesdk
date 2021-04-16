package com.spyneai.videorecording

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.telephony.ServiceState
import android.util.Log
import android.widget.Toast
import com.spyneai.R
import com.spyneai.aipack.FetchBulkResponse
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class UploadVideoService : Service() {

    private var TAG = "UploadService"
    private var isServiceStarted = false
    private var filePath = ""

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null) {
            val action = intent.action
            filePath = intent.getStringExtra("file_path").toString()

            when (action) {
                "START" -> startService()
                "STOP" -> stopService()
                else -> Log.d(TAG, "onStartCommand: This should never happen. No action in the received intent")
            }
        } else {

        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        var notification = createNotification()
        startForeground(1, notification)
    }

    private fun startService() {
        if (isServiceStarted) return
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true


        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    pingFakeServer()
                }
            }

        }
    }

    private fun stopService() {
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {

            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {

        }
        isServiceStarted = false

    }

    private fun pingFakeServer() {

        try {
            val request = RetrofitClients.buildService(APiService::class.java)

            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), File(filePath))
            val video = MultipartBody.Part.createFormData("video", File(filePath)!!.name, requestFile)

            val userId = RequestBody.create(
                MultipartBody.FORM,"sandeep singh"
            )

            val skuName = RequestBody.create(
                MultipartBody.FORM,"sku name"
            )

            val skuId = RequestBody.create(
                MultipartBody.FORM,"sku id"
            )

            val type = RequestBody.create(
                MultipartBody.FORM,"three sixty"
            )

            val category = RequestBody.create(
                MultipartBody.FORM,"automobiles"
            )

            val call = request.uploadVideo(video,userId,skuName,skuId,type,category)

            call?.enqueue(object : Callback<UploadVideoResponse> {
                override fun onResponse(
                    call: Call<UploadVideoResponse>,
                    response: Response<UploadVideoResponse>
                ) {
                    Log.d(TAG, "onResponse: success")
                }

                override fun onFailure(call: Call<UploadVideoResponse>, t: Throwable) {
                    Log.d(TAG, "onResponse: failure")
                }
            })



        } catch (e: Exception) {

        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, ThreeSixtyViewActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("Endless Service")
            .setContentText("This is your favorite endless service working")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }
}