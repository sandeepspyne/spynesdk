package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.base.network.Resource
import com.spyneai.base.network.ServerException
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ProcessRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException

class FrameUpdateWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "FrameUpdateWorker"
    val processRepository = ProcessRepository()

    override suspend fun doWork(): Result {
        if (runAttemptCount > 4) {
            captureEvent(Events.TOTAL_FRAMES_UPDATE_FAILED,false,"Frames updated limit  reached")
            return Result.failure()
        }


        var response = processRepository.updateTotalFrames(
            inputData.getString("auth_key")!!,
            inputData.getString("sku_id")!!,
            inputData.getString("total_frames")!!)

        when(response) {
            is Resource.Success -> {
                com.spyneai.shoot.utils.log("Frames updated success")
                captureEvent(Events.TOTAL_FRAMES_UPDATED,true,null)

                return Result.success()
            }

            is Resource.Failure -> {
                if(response.errorMessage == null){
                    captureEvent(Events.TOTAL_FRAMES_UPDATE_FAILED,false,response.errorCode.toString()+": Http exception from server")
                }else {
                    captureEvent(Events.TOTAL_FRAMES_UPDATE_FAILED,false,response.errorCode.toString()+": "+response.errorMessage)
                }

                Result.retry()
            }
        }

        return Result.success()
    }


    private fun captureEvent(eventName : String,isSuccess : Boolean, error: String?) {
        val properties = Properties()
        properties.apply {
            this["sku_id"] = inputData.getString("sku_id")
            this["total_frames"] = inputData.getString("total_frames")

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