package com.spyneai.shoot.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.spyneai.R
import com.spyneai.shoot.data.ShootLocalRepository

class LongRunningWorker(val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    val TAG = "LongRunningWorker"
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {

        val localRepository = ShootLocalRepository()

        val image = localRepository.getOldestImage()

        if (image.itemId != null){
            Log.d(TAG, "doWork: "+image.skuId)
            Log.d(TAG, "doWork: "+image.imagePath)


        }else{
            Log.d(TAG, "doWork: "+" Image null ")
        }

        val progress = "Starting Download"
        setForeground(createForegroundInfo(progress))

        return Result.success()
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = 10001
//        val title = applicationContext.getString(R.string.notification_title)
        val title = "Test"
       // val cancel = applicationContext.getString(R.string.cancel_download)
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, "CHANNEL_ID")
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.mipmap.app_logo)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            //.addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(id,notification)
    }


//    private fun createForegroundInfo(progress: String): ForegroundInfo {
//        // ...
//        return ForegroundInfo(NOTIFICATION_ID, notification,
//            FOREGROUND_SERVICE_TYPE_LOCATION or FOREGROUND_SERVICE_TYPE_MICROPHONE)
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val name = "my channnel"
        val descriptionText =  "my channnel description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system

        notificationManager.createNotificationChannel(channel)
    }



}