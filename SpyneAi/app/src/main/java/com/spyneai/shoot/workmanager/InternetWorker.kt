package com.spyneai.shoot.workmanager

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spyneai.captureEvent
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.isMyServiceRunning
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ShootLocalRepository

class InternetWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val shootLocalRepository = ImageLocalRepository()
        if (shootLocalRepository.getOldestImage("0").itemId != null
            || shootLocalRepository.getOldestImage("-1").itemId != null){
            if (!appContext.isMyServiceRunning(ImageUploadingService::class.java)){
                Utilities.saveBool(appContext, AppConstants.UPLOADING_RUNNING, false)

                capture(Events.SERVICE_STARTED,"Started")
                var action = Actions.START
                if (getServiceState(appContext) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
                    return Result.success()

                val serviceIntent = Intent(appContext, ImageUploadingService::class.java)
                serviceIntent.putExtra(AppConstants.SERVICE_STARTED_BY, InternetWorker::class.simpleName)
                serviceIntent.action = action.name

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    log("Starting the service in >=26 Mode")
                    ContextCompat.startForegroundService(appContext, serviceIntent)
                    return Result.success()
                } else {
                    log("Starting the service in < 26 Mode")
                    appContext.startService(serviceIntent)
                }
            }else {
                capture(Events.SERVICE_STARTED,"Running")
            }
        }

        return Result.success()
    }

    private fun capture(eventName : String,state : String) {
        val prperties = HashMap<String,Any?>()
            .apply {
                put("service_state",state)
                put("email",Utilities.getPreference(appContext,AppConstants.EMAIL_ID).toString())
                put("medium","Internet Worker")
            }

        appContext.captureEvent(eventName,prperties)
    }
}