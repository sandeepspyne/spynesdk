package com.spyneai.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import com.spyneai.R
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.dashboard.ui.dashboard.MainDashboardActivity
import com.spyneai.extras.events.ProcessingImagesEvent
import com.spyneai.model.processImageService.Task
import com.spyneai.needs.AppConstants
import org.greenrobot.eventbus.EventBus


class ProcessImagesService() : Service(), Listener {

    private var wakeLock: PowerManager.WakeLock? = null


    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder




    companion object {
        var tasksInProgress = ArrayList<Task>()
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null)
            return START_STICKY

        val action = intent.action
        log("using an intent with action $action")

        when (action) {
            Actions.START.name -> fetchDataAndStartService(intent)
            Actions.STOP.name -> stopService()
            else -> error("No action in the received intent")
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        log("The service has been destroyed".toUpperCase())
    }

    private fun fetchDataAndStartService(intent: Intent) {

        val task = Task()

        task.totalExteriorImages = 0
        task.totalInteriorImages = 0
        task.totalFocusedImages = 0

        task.skuName = intent.getStringExtra(AppConstants.SKU_NAME) ?: ""
        task.skuId = intent.getStringExtra(AppConstants.SKU_ID) ?: ""
        task.shootId = intent.getStringExtra(AppConstants.SHOOT_ID) ?: ""
        task.tokenId = intent.getStringExtra(AppConstants.tokenId) ?: ""
        task.windows = intent.getStringExtra(AppConstants.WINDOWS) ?: ""
        task.exposures = intent.getStringExtra(AppConstants.EXPOSURES) ?: ""

        task.catName = intent.getStringExtra(AppConstants.CATEGORY_NAME) ?: ""
        task.marketplaceId = intent.getStringExtra(AppConstants.MARKETPLACE_ID) ?: ""
        task.backgroundColour = intent.getStringExtra(AppConstants.BACKGROUND_COLOUR) ?: ""

        task.backgroundSelect = intent.getStringExtra(AppConstants.BG_ID) ?: ""

        task.imageFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        task.imageFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)
        task.totalImagesToUpload = task.imageFileList.size

        task.imageInteriorFileList.addAll(
            intent.getParcelableArrayListExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST)
                ?: ArrayList()
        )
        task.imageInteriorFileListFrames.addAll(
            intent.getIntegerArrayListExtra(AppConstants.ALL_INTERIOR_FRAME_LIST)
                ?: ArrayList()
        )
        task.imageFocusedFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_FOCUSED_IMAGE_LIST)!!)
        task.imageFocusedFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FOCUSED_FRAME_LIST)!!)
        task.dealershipLogo = intent.getStringExtra(AppConstants.DEALERSHIP_LOGO) ?: ""
        task.cornerPosition = intent.getStringExtra(AppConstants.CORNER_POSITION) ?: ""

        if (task.imageFileList.size > 0)
            task.totalExteriorImages = task.imageFileList.size
        else
            task.totalExteriorImages = 0
        if (task.imageInteriorFileList.size > 0)
        task.totalInteriorImages = task.imageInteriorFileList.size
        else
            task.totalInteriorImages = 0
        if (task.imageFocusedFileList.size > 0)
        task.totalFocusedImages = task.imageFocusedFileList.size
        else
            task.totalFocusedImages = 0



        tasksInProgress.add(task)
        checkAndFinishService()
        PhotoUploader(task, this).start()


    }

    private fun checkAndFinishService() {
        //clear all notifications

        notificationManager.cancelAll()

        tasksInProgress.forEach {

            if (it.isCompleted) {
                createCompletedNotification()
            } else if (it.isFailure) {
                createFailureNotification()
            } else
                createOngoingNotificaiton()
        }
        if (tasksInProgress.filter { !it.isCompleted || !it.isFailure }.isEmpty()) {
            stopService()
        }
    }

    private fun createOngoingNotificaiton() {
        var notificationId = (0..999999).random()
        val text: String = "Image processing in progress..."
        var notification = createNotification(text, true)

        notificationManager.notify(notificationId, notification)
        startForeground(notificationId, notification)

    }

    private fun createCompletedNotification() {
        var notification = createNotification("Image processing completed", false)
        notificationManager.notify((0..999999).random(), notification)
    }

    private fun createFailureNotification() {
        var notification = createNotification("Image processing Failed", false)
        notificationManager.notify((0..999999).random(), notification)
    }


    override fun onCreate() {
        super.onCreate()
        setServiceState(this, com.spyneai.service.ServiceState.STARTED)

        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ProcessService::lock").apply {
                acquire()
            }
        }

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


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
            var processingImageEvent = ProcessingImagesEvent();
            processingImageEvent.setShootStatus("fail")
            EventBus.getDefault().post(processingImageEvent)

            stopForeground(true)
            stopSelf()

        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
//        isServiceStarted = false
        setServiceState(this, com.spyneai.service.ServiceState.STOPPED)
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

    override fun onSuccess(task: Task) {
        tasksInProgress.remove(task)
        task.isCompleted = true
        stopService()
        checkAndFinishService()
    }

    override fun onFailure(task: Task) {
        task.isFailure = true
        stopService()
        //retry funcnality
        checkAndFinishService()
    }


}
