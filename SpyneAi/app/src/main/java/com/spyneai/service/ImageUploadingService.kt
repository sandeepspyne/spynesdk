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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ImageUploadingService : Service(),DataSyncListener {

    private var wakeLock: PowerManager.WakeLock? = null
    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var builder: Notification.Builder
    private var receiver: InternetConnectionReceiver? = null
    private var imageUploader : ImageUploader? = null
    private var prjSync: ProjectSkuSync? = null
    private var processSkuSync: ProcessSkuSync? = null
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

                when(intent.getSerializableExtra(AppConstants.SYNC_TYPE)){
                    ServerSyncTypes.CREATE -> {
                        startProjectSync("onStartCommand")
                    }

                    ServerSyncTypes.PROCESS -> {
                        startProcessSync("onStartCommand")
                    }

                    ServerSyncTypes.UPLOAD -> {
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

            //Utilities.saveBool(this, AppConstants.UPLOADING_RUNNING, false)
            //cancel all jobs started by service


        } catch (e: Exception) {
        }

        setServiceState(this, ServiceState.STOPPED)
    }



    override fun inProgress(title: String, type: ServerSyncTypes) {
        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val finalContent = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,finalContent, true)

        notificationManager.notify(notificationId, notification)

//        if (type == ServerSyncTypes.UPLOAD)
    }

    override fun onCompleted(title: String, type: ServerSyncTypes) {

        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,content, true)

        notificationManager.notify(notificationId, notification)

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


    override fun onConnectionLost(title: String, type: ServerSyncTypes) {
        captureEvent(Events.INTERNET_DISCONNECTED,
            HashMap<String,Any?>().apply {
                put("medium","Service")
            })

        val internet = if (isInternetActive()) getString(R.string.active) else getString(R.string.disconnected)
        val content = getString(R.string.innter_connection_label)+internet
        var notification = createNotification(title,content, true)

        notificationManager.notify(notificationId, notification)
    }

    inner class InternetConnectionReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val isConnected = context?.isInternetActive()

            if (isConnected == true){
                //push event of internet connected
                    captureEvent(
                        Events.INTERNET_CONNECTED,
                    HashMap<String,Any?>().apply {
                        put("medium","Service")
                    })

                prjSync?.connectionLost = false
                processSkuSync?.connectionLost = false
                imageUploader?.connectionLost = false

               GlobalScope.launch(Dispatchers.IO) {
                   val shootDao = AppDatabase.getInstance(BaseApplication.getContext()).shootDao()
                   val shootLocalRepository = ImagesRepoV2(shootDao)
                   if (shootLocalRepository.getOldestImage() != null
                   ) {

                       resumeUpload("onReceive")
                   }

                   val pendingProjects = shootDao.getPendingProjects()

                   if (pendingProjects > 0){
                       startProjectSync("onReceive")
                   }

                   val pendingSkus = shootDao.getPendingSku()

                   if (pendingSkus > 0){
                       startProcessSync("onReceive")
                   }
               }

            }else {
                //push event of internet not connected
                captureEvent(Events.INTERNET_DISCONNECTED,
                    HashMap<String,Any?>().apply {
                        put("medium","Service")
                    })

                prjSync?.connectionLost = true
                processSkuSync?.connectionLost = true
                imageUploader?.connectionLost = true
            }
        }
    }

    private fun resumeUpload(type : String) {
        imageUploader = ImageUploader.getInstance(this,this)
        imageUploader?.uploadParent(type,serviceStartedBy)
    }

    private fun startProjectSync(type: String) {
        prjSync = ProjectSkuSync.getInstance(this,this)

        prjSync?.projectSyncParent(type,serviceStartedBy)
    }


    private fun startProcessSync(type: String) {
        processSkuSync = ProcessSkuSync.getInstance(
            this, this)

        processSkuSync?.processSkuParent(type,serviceStartedBy)
    }

}