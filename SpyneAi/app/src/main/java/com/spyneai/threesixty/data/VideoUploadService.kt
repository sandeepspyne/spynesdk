package com.spyneai.threesixty.data

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.*
import com.spyneai.R
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.isInternetActive
import com.spyneai.service.Actions
import com.spyneai.service.ServiceState
import com.spyneai.service.log
import com.spyneai.service.setServiceState
import com.spyneai.shoot.utils.logUpload
import com.spyneai.threesixty.data.model.VideoDetails

class VideoUploadService : Service(), VideoUploader.Listener {
    private var wakeLock: PowerManager.WakeLock? = null
    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder
    private var receiver: VideoConnectionReceiver? = null
    var uploadRunning = false
    var isConnected = false
    private var imageUploader : VideoUploader? = null
    private var notificationId = 0
    val notificationChannelId = "PROCESSING SERVICE CHANNEL"
    var currentVideo : VideoDetails? = null

    override fun onDestroy() {
        super.onDestroy()
        if (receiver != null)
            unregisterReceiver(receiver)
    }

    override fun onCreate() {
        super.onCreate()
        setServiceState(this, ServiceState.STARTED)

        //register internet connection receiver
        this.receiver = VideoConnectionReceiver()
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        this.registerReceiver(receiver, filter)

        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ProcessService::lock").apply {
                acquire()
            }
        }

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fetchDataAndStartService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            return START_STICKY

        val action = intent.action
        log("using an intent with action $action")

        when (action) {
            Actions.START.name -> {
                if (!uploadRunning)
                    resumeUpload("onStartCommand")
            }
            Actions.STOP.name -> stopService()
            else -> error("No action in the received intent")
        }

        return START_STICKY
    }

    private fun fetchDataAndStartService() {
        logUpload("Service Started")
        createOngoingNotificaiton()
    }

    private fun createOngoingNotificaiton() {
        notificationId = 102
        val title = getString(R.string.app_name)
        val text = "Video uploading in progress..."
        var notification = createNotification(title,text, true)

        logUpload("createOngoingNotificaiton "+notificationId)
        notificationManager.notify(notificationId, notification)
        startForeground(notificationId, notification)
    }

    private fun createNotification(title: String,text: String, isOngoing: Boolean): Notification {
        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                notificationChannelId,
                "Process Video Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Process Images Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(false)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        var pendingIntent: PendingIntent = Intent(this, MainDashboardActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else
                Notification.Builder(this)

        return builder
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.app_logo)
            .setOngoing(isOngoing)
            .setAutoCancel(isOngoing)
            .setOnlyAlertOnce(true)
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    private fun stopService() {
        log("Stopping the foreground service")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }

            stopForeground(false)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
//        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    override fun inProgress(task: VideoDetails) {
        currentVideo = task
        logUpload("in progress "+task.skuName)
        val category = if (task.categoryName == "Focus Shoot") "Miscellaneous" else task.categoryName
        val title = "Uploading "+task.skuName
        val internet = if (isInternetActive()) "Active" else "Disconnected"
        val content = "Internet Connection: "+internet
        var notification = createNotification(title,content, true)

        logUpload("inProgress "+notificationId)
        notificationManager.notify(notificationId, notification)
        uploadRunning = true
    }

    override fun onUploaded(task: VideoDetails) {
        uploadRunning = false

        var title = "Video Uploaded"
        if (currentVideo != null){
            title = "Last Uploaded "+currentVideo?.skuName
            logUpload("uploaded "+currentVideo?.videoPath)
        }

        val internet = if (isInternetActive()) "Active" else "Disconnected"
        val content = "Internet Connection: "+internet
        var notification = createNotification(title,content, true)

        logUpload("onUploaded last "+notificationId)
        notificationManager.notify(notificationId, notification)

        //update notification after five minutes
        Handler(Looper.getMainLooper()).postDelayed({
            val title = "All Videos Uploaded "
            val internet = if (isInternetActive()) "Active" else "Disconnected"
            val content = "Internet Connection: "+internet
            var notification = createNotification(title,content, false)

            logUpload("onUploaded all "+notificationId)
            notificationManager.notify(notificationId, notification)
            stopService()
        },180000)
    }

    override fun onUploadFail(task: VideoDetails) {
        uploadRunning = false
    }

    override fun onConnectionLost() {
        logUpload("onConnectionLost")
        uploadRunning = false

        val title = if (currentVideo == null) "Uploading Paused"
        else {
            val category = if (currentVideo?.categoryName == "Focus Shoot") "Miscellaneous" else currentVideo?.categoryName
            "Uploading Paused On "+currentVideo?.skuName
        }
        val content = "Internet Connection: Disconnected"
        var notification = createNotification(title,content, true)

        logUpload("onConnectionLost "+notificationId)
        notificationManager.notify(notificationId, notification)
    }

    inner class VideoConnectionReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val isConnected = context?.isInternetActive()

            logUpload("Connection changed "+isConnected)

            if (isConnected == true){
                //if any image pending in upload
                val image = VideoLocalRepository().getOldestVideo("0")

                if (image.itemId != null && !uploadRunning){
                    uploadRunning = true
                    logUpload(image.itemId.toString()+" "+uploadRunning)
                    // we have pending images, resume upload
                    resumeUpload("onReceive")
                }
            }
        }
    }

    private fun resumeUpload(type : String) {
        logUpload("RESUME UPLOAD "+type)

        uploadRunning = true

        if (imageUploader == null)
            imageUploader = VideoUploader(this,
                VideoLocalRepository(),
                ThreeSixtyRepository(),
                this
            )

        imageUploader!!.start()
    }

}