package com.spyneai.shoot.workmanager

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spyneai.*
import com.spyneai.base.room.AppDatabase
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.*
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ImagesRepoV2


class InternetWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        appContext.checkPendingDataSync()

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