package com.spyneai.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.*
import android.util.Log
import com.spyneai.*
import com.spyneai.base.room.AppDatabase
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ImagesRepoV2
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.repository.model.image.Image


class ImageUploadingService : Service(), ImageUploader.Listener,DataSyncListener {

    private var wakeLock: PowerManager.WakeLock? = null
    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder
    private var receiver: InternetConnectionReceiver? = null
    var uploadRunning = false
    private var imageUploader : ImageUploader? = null
    private var notificationId = 0
    val notificationChannelId = "PROCESSING SERVICE CHANNEL"
    var currentImage : Image? = null
    val TAG = "ImageUploader"
    var serviceStartedBy : String? = null

    override fun onDestroy() {
        super.onDestroy()
        if (receiver != null)
            unregisterReceiver(receiver)
    }

    override fun onCreate() {
        super.onCreate()
        setServiceState(this, ServiceState.STARTED)

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

        when (action) {
            Actions.START.name -> {
                this.serviceStartedBy = intent.getStringExtra(AppConstants.SERVICE_STARTED_BY)
                val shootDao = AppDatabase.getInstance(BaseApplication.getContext()).shootDao()

                when(intent.getSerializableExtra(AppConstants.SYNC_TYPE)){
                    SeverSyncTypes.CREATE -> {
                        val prjSync = ProjectSkuSync(
                            this,
                            shootDao,
                            this
                        )

                        prjSync.projectSyncParent("Image Uploading Service",serviceStartedBy)
                    }

                    SeverSyncTypes.PROCESS -> {
                        val processSkuSync = ProcessSkuSync(
                            this,
                            shootDao,
                            this
                        )

                        processSkuSync.processSkuParent("Image Uploading Service",serviceStartedBy)
                    }

                    SeverSyncTypes.UPLOAD -> {
                        if (!uploadRunning){
                            val properties = java.util.HashMap<String, Any?>()
                                .apply {
                                    put("service_state", "Started")
                                    put("medium", "Image Uploading Service")
                                }

                            captureEvent(Events.SERVICE_STARTED, properties)
                            resumeUpload("onStartCommand")
                        }
                    }
                }







            }
            Actions.STOP.name -> stopService()
            else -> error("No action in the received intent")
        }

        return START_STICKY
    }

    private fun fetchDataAndStartService() {
        createOngoingNotificaiton()
    }

    private fun createOngoingNotificaiton() {
        notificationId = 100
        val title = getString(R.string.app_name)
        val text = getString(R.string.image_uploading_in_progess)
        var notification = createNotification(title,text, true)

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
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }

            stopForeground(false)
            stopSelf()

            Utilities.saveBool(this, AppConstants.UPLOADING_RUNNING, false)
            //cancel all jobs started by service
            imageUploader?.job?.cancel()

        } catch (e: Exception) {
        }

        setServiceState(this, ServiceState.STOPPED)
    }

    override fun inProgress(task: Image) {
        currentImage = task
        val category = if (task.image_category == "Focus Shoot") getString(R.string.miscellanous) else task.image_category
        val title = getString(R.string.upload)+task.skuName+"("+category+"-"+task.sequence+")"
        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,content, true)

        notificationManager.notify(notificationId, notification)
       uploadRunning = true
    }

    override fun onUploaded() {
        uploadRunning = false

        var title = getString(R.string.image_uploaded)
        if (currentImage != null){
            val category = if (currentImage?.image_category == "Focus Shoot") getString(R.string.miscellanous) else currentImage?.image_category
            title = getString(R.string.last_uploaded)+currentImage?.skuName+"("+category+"-"+currentImage?.sequence+")"
        }

        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,content, true)

        notificationManager.notify(notificationId, notification)

        //update notification after five minutes
        Handler(Looper.getMainLooper()).postDelayed({
            val title = getString(R.string.all_uploaded)
            val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
            val content = getString(R.string.innter_connection_label)+internet
            var notification = createNotification(title,content, false)

            notificationManager.notify(notificationId, notification)
            stopService()
        },180000)
    }

    override fun onUploadFail(task: Image) {
        uploadRunning = false
    }

    override fun inProgress(title: String, type: SeverSyncTypes) {
        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val finalContent = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,finalContent, true)

        notificationManager.notify(notificationId, notification)

        if (type == SeverSyncTypes.UPLOAD)
            uploadRunning = true
    }

    override fun onCompleted(title: String,
                             type: SeverSyncTypes,
                             stopService: Boolean) {

        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,content, true)

        notificationManager.notify(notificationId, notification)

        if (type == SeverSyncTypes.UPLOAD)
            uploadRunning = false

        //update notification after five minutes
        if (allDataSynced()){
            Handler(Looper.getMainLooper()).postDelayed({
                val title = getString(R.string.all_uploaded)
                val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
                val content = getString(R.string.innter_connection_label)+internet
                var notification = createNotification(title,content, false)

                notificationManager.notify(notificationId, notification)
                stopService()
            },180000)
        }
    }



    override fun onConnectionLost(title: String, type: SeverSyncTypes) {
        captureEvent(Events.INTERNET_DISCONNECTED,
            HashMap<String,Any?>().apply {
                put("medium","Service")
            })

        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,content, true)

        notificationManager.notify(notificationId, notification)

        if (type == SeverSyncTypes.UPLOAD)
            uploadRunning = false
    }

    override fun onConnectionLost() {
        uploadRunning = false

        captureEvent(Events.INTERNET_DISCONNECTED,
            HashMap<String,Any?>().apply {
                put("medium","Service")
            })

        val title = if (currentImage == null) getString(R.string.uploading_paused)
        else {
            val category = if (currentImage?.image_category == "Focus Shoot") getString(R.string.miscellanous) else currentImage?.image_category
            getString(R.string.uploading_paused_on)+currentImage?.skuName+"("+category+"-"+currentImage?.sequence+")"
        }
        val content = getString(R.string.internet_connection)
        var notification = createNotification(title,content, true)

        notificationManager.notify(notificationId, notification)
    }

    inner class InternetConnectionReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val isConnected = context?.isInternetActive()
            Log.d(TAG, "onReceive: "+isConnected)

            if (isConnected == true){
                //push event of internet connected
                    captureEvent(
                        Events.INTERNET_CONNECTED,
                    HashMap<String,Any?>().apply {
                        put("medium","Service")
                    })
                //if any image pending in upload
                val shootLocalRepository = ImageLocalRepository()
                if ((shootLocalRepository.getOldestImage("0").itemId != null
                            || shootLocalRepository.getOldestImage("-1").itemId != null)
                    && !uploadRunning){
                    // we have pending images, resume upload
                    resumeUpload("onReceive")
                }
            }else {
                //push event of internet not connected
                captureEvent(Events.INTERNET_DISCONNECTED,
                    HashMap<String,Any?>().apply {
                        put("medium","Service")
                    })

                imageUploader?.connectionLost = true
            }
        }
    }

    private fun resumeUpload(type : String) {
        Log.d(TAG, "resumeUpload: "+type)
        uploadRunning = true

        if (imageUploader == null)
            imageUploader = ImageUploader(this,
                ImagesRepoV2(AppDatabase.getInstance(BaseApplication.getContext()).shootDao()),
                ShootRepository(),
                this
            )

        imageUploader?.connectionLost = false
        imageUploader?.uploadParent(type,serviceStartedBy)
    }

}