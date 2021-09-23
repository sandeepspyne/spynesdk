package com.spyneai.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import com.spyneai.R
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.model.processImageService.Task
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import android.content.IntentFilter
import android.icu.text.CaseMap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.*
import androidx.core.content.ContextCompat
import com.spyneai.captureEvent
import com.spyneai.isInternetActive
import com.spyneai.isMyServiceRunning
import com.spyneai.shoot.utils.logUpload
import java.io.File


class ImageUploadingService : Service(), ImageUploader.Listener {

    private var wakeLock: PowerManager.WakeLock? = null
    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder
    private var receiver: InternetConnectionReceiver? = null
    var uploadRunning = false
    var isConnected = false
    private var imageUploader : ImageUploader? = null
    private var notificationId = 0
    val notificationChannelId = "PROCESSING SERVICE CHANNEL"
    var currentImage : Image? = null

    override fun onDestroy() {
        super.onDestroy()
        if (receiver != null)
            unregisterReceiver(receiver)
    }

    override fun onCreate() {
        super.onCreate()
        setServiceState(this, com.spyneai.service.ServiceState.STARTED)

        //register internet connection receiver
        this.receiver = InternetConnectionReceiver()
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
        notificationId = 100
        val title = getString(R.string.app_name)
        val text = getString(R.string.image_uploading_in_progess)
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
                "Process Images Service notifications channel",
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
        setServiceState(this, com.spyneai.service.ServiceState.STOPPED)
    }

    override fun inProgress(task: Image) {
        currentImage = task
        logUpload("in progress "+task.imagePath)
        val category = if (task.categoryName == "Focus Shoot") getString(R.string.miscellanous) else task.categoryName
        val title = getString(R.string.upload)+task.skuName+"("+category+"-"+task.sequence+")"
        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,content, true)

        logUpload("inProgress "+notificationId)
        notificationManager.notify(notificationId, notification)
       uploadRunning = true
    }

    override fun onUploaded(task: Image) {
        uploadRunning = false

        var title = getString(R.string.image_uploaded)
        if (currentImage != null){
            val category = if (currentImage?.categoryName == "Focus Shoot") getString(R.string.miscellanous) else currentImage?.categoryName
            title = getString(R.string.last_uploaded)+currentImage?.skuName+"("+category+"-"+currentImage?.sequence+")"
            logUpload("uploaded "+currentImage?.imagePath)
        }

        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,content, true)

        logUpload("onUploaded last "+notificationId)
        notificationManager.notify(notificationId, notification)

        //update notification after five minutes
        Handler(Looper.getMainLooper()).postDelayed({
            val title = getString(R.string.all_uploaded)
            val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
            val content = getString(R.string.innter_connection_label)+internet
            var notification = createNotification(title,content, false)

            logUpload("onUploaded all "+notificationId)
            notificationManager.notify(notificationId, notification)
            stopService()
        },180000)
    }

    override fun onUploadFail(task: Image) {
        uploadRunning = false
    }

    override fun onConnectionLost() {
        logUpload("onConnectionLost")
        uploadRunning = false

        val title = if (currentImage == null) getString(R.string.uploading_paused)
        else {
            val category = if (currentImage?.categoryName == "Focus Shoot") getString(R.string.miscellanous) else currentImage?.categoryName
            getString(R.string.uploading_paused_on)+currentImage?.skuName+"("+category+"-"+currentImage?.sequence+")"
        }
        val content = getString(R.string.internet_connection)
        var notification = createNotification(title,content, true)

        logUpload("onConnectionLost "+notificationId)
        notificationManager.notify(notificationId, notification)
    }

    inner class InternetConnectionReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val isConnected = context?.isInternetActive()

            logUpload("Connection changed "+isConnected)


            if (isConnected == true){
                //if any image pending in upload
                val image = ShootLocalRepository().getOldestImage()

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
            imageUploader = ImageUploader(this,
                ShootLocalRepository(),
                ShootRepository(),
                this
            )

        imageUploader!!.start()
    }

}