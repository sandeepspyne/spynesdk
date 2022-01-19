package com.spyneai.shoot.workmanager

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spyneai.BaseApplication
import com.spyneai.base.room.AppDatabase
import com.spyneai.captureEvent
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.isMyServiceRunning
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.*
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ImagesRepoV2
import com.spyneai.startUploadingService


class InternetWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val shootDao = AppDatabase.getInstance(BaseApplication.getContext()).shootDao()
        val shootLocalRepository = ImagesRepoV2(shootDao)
        if (shootLocalRepository.getOldestImage() != null
        ) {
//            if (!appContext.isMyServiceRunning(ImageUploadingService::class.java))
//                Utilities.saveBool(appContext, AppConstants.UPLOADING_RUNNING, false)

            appContext.startUploadingService(
                MainDashboardActivity::class.java.simpleName,
                ServerSyncTypes.UPLOAD
            )
        }

        val pendingProjects = shootDao.getPendingProjects()

        if (pendingProjects > 0){
            appContext.startUploadingService(
                MainDashboardActivity::class.java.simpleName,
                ServerSyncTypes.CREATE
            )
        }

        val pendingSkus = shootDao.getPendingSku()

        if (pendingSkus > 0){
            appContext.startUploadingService(
                MainDashboardActivity::class.java.simpleName,
                ServerSyncTypes.PROCESS
            )
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