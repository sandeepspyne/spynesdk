package com.spyneai.videorecording.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.spyneai.R
import com.spyneai.videorecording.SpinViewActivity
import com.spyneai.videorecording.ThreeSixtyInteriorViewActivity
import com.spyneai.videorecording.ThreeSixtyViewActivity
import com.spyneai.videorecording.model.VideoTask


class UploadVideoService : Service(), VideoUploader.VideoTaskListener {

    private var wakeLock: PowerManager.WakeLock? = null

    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder
    lateinit var filePath : String
    var shootMode : Int = 0

    var tasksInProgress = ArrayList<VideoTask>()
    var frams = ArrayList<String>()
    var TAG = "UploadVideoService"
    var skuId = ""
    var processedSkuId = ""

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            return START_STICKY

        val action = intent.action
        shootMode = intent.getIntExtra("shoot_mode",0)
        filePath = intent.getStringExtra("file_path").toString()
        skuId = intent.getStringExtra("sku_id")!!
        Log.d(TAG, "onStartCommand: "+shootMode+" "+skuId)

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
        task.shootMode = shootMode
        task.skuId = skuId

        if (shootMode == 1 && FramesHelper.videoUrlMap.get(skuId) != null)
            task.videoUrl = FramesHelper.videoUrlMap.get(skuId)!!

        FramesHelper.taskMap.put(skuId,task)
        tasksInProgress.add(task)

        checkAndFinishService()

        if (shootMode == 0) VideoUploader(task,this).uploadVideo()
        else {
            if (FramesHelper.videoUrlMap.get(skuId) != null){
                FramesHelper.processingMap.put(skuId,true)
                VideoUploader(task,this).processVideo()
            }else{
                Log.d(TAG, "startService: video uploading in progress")
            }

        }
    }

    private fun stopService() {
        // Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
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
                if (it.shootMode == 1)
                    processedSkuId = it.skuId

                createCompletedNotification(it.shootMode)
            } else if (it.onFailure) {
                createFailureNotification(it.shootMode)
            } else
                createOngoingNotificaiton(it.shootMode)
        }

        if (tasksInProgress.filter { !it.isCompleted }.isEmpty()) {
            stopForeground(true)
            stopSelf()
        }
    }


    private fun createVideoUploadingNotification() {
        var notificationId = (0..999999).random()
        val text: String = "Video Uploading in progress..."
        var notification = createNotification(text , true,shootMode)

        notificationManager.notify(notificationId, notification)
        startForeground(notificationId, notification)
    }

    private fun createOngoingNotificaiton(shootMode: Int) {
        var notificationId = (0..999999).random()
        val text: String = "Video processing in progress..."
        var notification = createNotification(text , true,shootMode)

        notificationManager.notify(notificationId, notification)
        startForeground(notificationId, notification)

    }

    private fun createCompletedNotification(shootMode: Int) {
        var notification = createNotification("Video processing completed", false,shootMode)
        notificationManager.notify((0..999999).random(), notification)
    }

    private fun createFailureNotification(shootMode: Int) {
        var notification = createNotification("Video processing failed.", false,shootMode)
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

    private fun createNotification(text: String, isOngoing: Boolean,shootMode : Int): Notification {
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

        var pendingIntent: PendingIntent

        if (shootMode == 0){
            pendingIntent = Intent(this, ThreeSixtyViewActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }
        }
        else{

            // Create an Intent for the activity you want to start
            val resultIntent = Intent(baseContext, ThreeSixtyInteriorViewActivity::class.java)

            resultIntent.setAction(processedSkuId)

            pendingIntent = PendingIntent.getActivity(this,0,resultIntent,0)

//            pendingIntent = Intent(resultIntent).let { notificationIntent ->
//                notificationIntent.putExtra("frames",frams)
//                notificationIntent.putExtra("sandeep","singh")
//
//                PendingIntent.getActivity(this, 0, notificationIntent, 0)
//            }



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

        if (task.shootMode == 1) {
            processedSkuId = task.skuId
            Log.d(TAG, "onSuccess: skuid"+processedSkuId)
            frams = task.frames as ArrayList<String>
            FramesHelper.hashMap.put(task.skuId,frams)
        }else {
            FramesHelper.videoUrlMap.put(task.skuId,task.responseUrl)

            //start processing if not started
            if (FramesHelper.processingMap.get(task.skuId) == null || FramesHelper.processingMap.get(task.skuId) == false){
                var pendingProcessing =  FramesHelper.taskMap.get(task.skuId)
                pendingProcessing?.videoUrl = FramesHelper.videoUrlMap.get(task.skuId).toString()

                FramesHelper.processingMap.put(skuId,true)
                if (pendingProcessing != null) {
                    Log.d(TAG, "startService: video completed processing started")
                    VideoUploader(pendingProcessing,this).processVideo()
                }
            }
        }

        checkAndFinishService()
    }

    override fun onFailure(task: VideoTask) {

    }
}