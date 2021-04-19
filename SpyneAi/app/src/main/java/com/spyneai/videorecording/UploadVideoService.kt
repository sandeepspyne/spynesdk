package com.spyneai.videorecording

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
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


class UploadVideoService : Service(), VideoUploader.VideoTaskListener {

    private var wakeLock: PowerManager.WakeLock? = null


    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder
    lateinit var filePath : String

    var tasksInProgress = ArrayList<VideoTask>()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            return START_STICKY

        val action = intent.action
        filePath = intent.getStringExtra("file_path").toString()


        when (action) {
            "START" -> startService()
            "STOP"  -> stopService()
            else -> error("No action in the received intent")
        }

        return START_STICKY

    }


    private fun startService() {
        val task = VideoTask()

        task.filePath = filePath

        tasksInProgress.add(task)

        checkAndFinishService()

        VideoUploader(task,this).uploadVideo()
    }

    private fun stopService() {
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

        }

    }

    private fun checkAndFinishService() {
        //clear all notifications

        notificationManager.cancelAll()

        tasksInProgress.forEach {

            if (it.isCompleted) {
                createCompletedNotification()
            } else if (it.onFailure) {
                createFailureNotification()
            } else
                createOngoingNotificaiton()

        }

        if (tasksInProgress.filter { !it.isCompleted }.isEmpty()) {
            stopForeground(true)
            stopSelf()
        }
    }

    private fun createOngoingNotificaiton() {
        var notificationId = (0..999999).random()
        val text: String = "Video processing in progress..."
        var notification = createNotification(text , true)

        notificationManager.notify(notificationId, notification)
        startForeground(notificationId, notification)

    }

    private fun createCompletedNotification() {
        var notification = createNotification("Video processing completed", false)
        notificationManager.notify((0..999999).random(), notification)
    }

    private fun createFailureNotification() {
        var notification = createNotification("Video processing failed.", false)
        notificationManager.notify((0..999999).random(), notification)
    }

    override fun onCreate() {
        super.onCreate()

        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ProcessService::lock").apply {
                acquire()
            }
        }

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    }

    private fun createNotification(text: String, isOngoing: Boolean): Notification {
        val notificationChannelId = "PROCESSING VIDEO SERVICE CHANNEL"
        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                notificationChannelId,
                "Upload Video Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Upload Video Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, ThreeSixtyViewActivity::class.java).let { notificationIntent ->
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
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.app_logo)
            .setOngoing(isOngoing)
            .setOnlyAlertOnce(true)
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()


    }


    override fun onSuccess(task: VideoTask) {
            task.isCompleted = true
            stopService()
    }

    override fun onFailure(task: VideoTask) {

    }
}