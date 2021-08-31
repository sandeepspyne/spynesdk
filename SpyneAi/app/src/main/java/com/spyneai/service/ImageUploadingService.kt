package com.spyneai.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
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
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.core.content.ContextCompat
import com.spyneai.isInternetActive
import com.spyneai.isMyServiceRunning
import com.spyneai.shoot.utils.logUpload


class ImageUploadingService : Service(), ImageUploader.Listener {

    private var wakeLock: PowerManager.WakeLock? = null
    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder
    private var receiver: InternetConnectionReceiver? = null
    var uploadRunning = false
    var isConnected = false
    private var imageUploader : ImageUploader? = null


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
            Actions.START.name,Actions.RUNNING.name -> {
                if (!uploadRunning)
                    resumeUpload()
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
        var notificationId = (0..999999).random()
        val text: String = "Image processing in progress..."
        var notification = createNotification(text, true)

        notificationManager.notify(notificationId, notification)
        startForeground(notificationId, notification)

    }

    private fun createNotification(text: String, isOngoing: Boolean): Notification {
        val notificationChannelId = "PROCESSING SERVICE CHANNEL"
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
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        var pendingIntent: PendingIntent? = null

        if (text.equals("Image processing in progress...")){
            pendingIntent =
                Intent(this, OngoingOrdersActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }
        }else if (text.equals("Image processing completed")){
            Intent(this, CompletedProjectsActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }
        }else{
            Intent(this, MainDashboardActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }
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
//        isServiceStarted = false
        setServiceState(this, com.spyneai.service.ServiceState.STOPPED)
    }

    override fun inProgress(task: Image) {
       uploadRunning = true
    }

    override fun onUploaded(task: Image) {
        uploadRunning = false
    }

    override fun onUploadFail(task: Image) {
        uploadRunning = false
    }

    override fun onConnectionLost() {
        logUpload("onConnectionLost")
        uploadRunning = false
    }

    inner class InternetConnectionReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val isConnected = context?.isInternetActive()

            logUpload("Connection changed "+isConnected)

            if (isConnected == true){
                //if any image pending in upload
                val image = ShootLocalRepository().getOldestImage()


                if (image.itemId != null && !uploadRunning){
                    logUpload(image.itemId.toString()+" "+uploadRunning)
                    // we have pending images, resume upload
                    resumeUpload()
                }
            }
        }
    }

    private fun resumeUpload() {
        logUpload("RESUME UPLOAD")
        if (imageUploader == null)
            imageUploader = ImageUploader(this,
                ShootLocalRepository(),
                ShootRepository(),
                this
            )

        imageUploader!!.start()
    }

}