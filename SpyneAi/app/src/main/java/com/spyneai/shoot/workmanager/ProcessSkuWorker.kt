package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.base.network.ClipperApi
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ProcessRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.model.ProcessSkuRes
import com.spyneai.shoot.data.model.UploadImageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProcessSkuWorker (val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val localRepository = ShootLocalRepository()

    override suspend fun doWork(): Result {
        if (runAttemptCount > 0) return Result.failure()
        processSku()
        return Result.success()
    }

    private  fun processSku() {
        try {

            val skuId = inputData.getString("skuId").toString()
            val authKey = inputData.getString("authKey").toString()
            val backgroundId = localRepository.getBackgroundId(skuId)

            //process SKU
            com.spyneai.shoot.utils.log("Process SKU started")
            com.spyneai.shoot.utils.log("Sku Id: "+skuId)
            com.spyneai.shoot.utils.log("Auth Key: "+authKey)
            com.spyneai.shoot.utils.log("Background Id: "+backgroundId)


            val call = RetrofitClients.buildService(ClipperApi::class.java)
                .processSkuWithWorker(authKey,skuId,backgroundId)

            call.enqueue(object : Callback<ProcessSkuRes> {
                override fun onResponse(
                    call: Call<ProcessSkuRes>,
                    response: Response<ProcessSkuRes>
                ) {
                    if (response.isSuccessful) {

                        val processSkuRes = response.body()

                        if (processSkuRes?.status == 200){
                            appContext.captureEvent(Events.PROCESS,Properties().putValue("sku_id",inputData.getString("skuId").toString()))
                        }else{
                            appContext.captureFailureEvent(
                                Events.PROCESS_FAILED,
                                Properties().putValue("sku_id",inputData.getString("skuId").toString()),
                                processSkuRes?.message!!)

                            processSku()

                        }
                    }else {
                        processSku()
                        com.spyneai.shoot.utils.log("processing not started yet")
                    }
                }

                override fun onFailure(call: Call<ProcessSkuRes>, t: Throwable) {
                    com.spyneai.shoot.utils.log("Upload image failed")
                    com.spyneai.shoot.utils.log("Error: " + t.localizedMessage)

                    appContext.captureFailureEvent(
                        Events.PROCESS_FAILED,
                        Properties().putValue("sku_id",inputData.getString("skuId").toString()),
                        t.localizedMessage)

                    processSku()
                }

            })

            appContext.captureEvent(
                Events.PROCESS,
                Properties().putValue("sku_id",inputData.getString("skuId").toString())
                    .putValue("background_id",backgroundId)
            )

        } catch (e : Exception) {
            com.spyneai.shoot.utils.log("Process Sku Exception: "+e.localizedMessage)
            e.printStackTrace()
        }
    }
}