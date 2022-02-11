package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spyneai.*
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities


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