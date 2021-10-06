package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.posthog.android.Properties
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootRepository

class ProjectStateUpdateWorker (private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "FrameUpdateWorker"
    val processRepository = ShootRepository()

    override suspend fun doWork(): Result {
        if (runAttemptCount > 4) {
            captureEvent(Events.PROJECT_STATE_UPDATE_FAILED,false,"Frames updated limit  reached")
            return Result.failure()
        }


        var response = processRepository.skuProcessState(
            inputData.getString("auth_key")!!,
            inputData.getString("project_id")!!)

        when(response) {
            is Resource.Success -> {
                com.spyneai.shoot.utils.log("Frames updated success")
                captureEvent(Events.PROJECT_STATE_UPDATED,true,null)

                return Result.success()
            }

            is Resource.Failure -> {
                if(response.errorMessage == null){
                    captureEvent(Events.PROJECT_STATE_UPDATE_FAILED,false,response.errorCode.toString()+": Http exception from server")
                }else {
                    captureEvent(Events.PROJECT_STATE_UPDATE_FAILED,false,response.errorCode.toString()+": "+response.errorMessage)
                }

                Result.retry()
            }
        }

        return Result.success()
    }


    private fun captureEvent(eventName : String,isSuccess : Boolean, error: String?) {
        val properties = Properties()
        properties.apply {
            this["project_id"] = inputData.getString("project_id")
            this["state"] = "Draft to Ongoing"

        }

        if (isSuccess) {
            appContext.captureEvent(
                eventName,
                properties)
        }else{
            appContext.captureFailureEvent(
                eventName,
                properties, error!!
            )
        }
    }
}