package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.base.network.ServerException
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ProcessRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.ProcessSkuRes
import com.spyneai.shoot.data.model.UploadImageResponse
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class ProcessSkuWorker (val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val localRepository = ShootLocalRepository()
    val processRepository = ProcessRepository()

    override suspend fun doWork(): Result {

        val skuId = inputData.getString("skuId").toString()
        val authKey = inputData.getString("authKey").toString()
        val backgroundId = localRepository.getBackgroundId(skuId)

        var properties = Properties()

        properties.apply {
            this["sku_id"] = inputData.getString("skuId").toString()
            this["background_id"] = backgroundId
        }

        if (runAttemptCount > 8) {
            captureEvent(Events.PROCESS_FAILED,properties,false,"Process Sku limit reached")
            return Result.failure()
        }


        //process SKU
        com.spyneai.shoot.utils.log("Process SKU started")
        com.spyneai.shoot.utils.log("Sku Id: "+skuId)
        com.spyneai.shoot.utils.log("Auth Key: "+authKey)
        com.spyneai.shoot.utils.log("Background Id: "+backgroundId)

        var jobs : Deferred<Resource<Any>>?

        coroutineScope {
            jobs =
                async {
                    processRepository.processSku(authKey,skuId, backgroundId)
                }

            jobs!!.await()
        }

        return if (jobs?.getCompleted() is Resource.Sucess) {
            com.spyneai.shoot.utils.log("Processed sku success")
            captureEvent(Events.PROCESS,properties,true,null)
            Result.success()
        }else{
            com.spyneai.shoot.utils.log("process sku failed")
            val throwable = jobs?.getCompletionExceptionOrNull()
            var error = ""

            when(throwable) {
                is ServerException -> {
                    if (throwable.message != null)
                        error = throwable.message.toString()
                }

                is HttpException -> {
                    val serverError = throwable.response()?.errorBody().toString()
                    if (serverError != null)
                        error = serverError
                }

                else -> {
                    error = "Request failed due to internet connection"
                }
            }


            captureEvent(Events.PROCESS_FAILED,properties,false,error)
            Result.retry()
        }
    }


    private fun captureEvent(eventName : String,properties : Properties,isSuccess : Boolean,error: String?) {

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