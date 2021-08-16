package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ProcessRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.data.model.Sku
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RecursiveProcessSkuWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "RecursiveImageWorker"
    val localRepository = ShootLocalRepository()
    val processRepository = ProcessRepository()

    override suspend fun doWork(): Result {

        val image = localRepository.getLastSku()

        if (runAttemptCount > 4) {
            if (image.itemId != null)
            // localRepository.deleteImage(image.itemId!!)

            //captureEvent(Events.UPLOAD_FAILED,image,false,"Image upload limit  reached")
                return Result.failure()
        }


        if (image.itemId != null){

            com.spyneai.shoot.utils.log("image selected "+image.itemId + " "+image.skuId)

            val skuId = image.skuId
            val backgroundId = image.backgroundId

            if (backgroundId != null){
                val authKey =
                    Utilities.getPreference(appContext, AppConstants.AUTH_KEY).toString()

                val is360 = image.is360 != -1

                var response = processRepository.processSku(authKey,skuId!!,backgroundId!!,is360)

                when(response){
                    is Resource.Success -> {
                        captureEvent(Events.PROCESS,image,true,null)
                        startNextUpload(image)
                        return Result.success()
                    }

                    is Resource.Failure -> {
                        if(response.errorMessage == null){
                            captureEvent(Events.PROCESS_FAILED,image,false,response.errorCode.toString()+": Http exception from server")
                        }else {
                            captureEvent(Events.PROCESS_FAILED,image,false,response.errorCode.toString()+": "+response.errorMessage)
                        }
                        return Result.retry()
                    }
                }
            }else{
                startNextUpload(image)
            }

            return Result.success()
        }else{
            return Result.success()
        }
    }

    private fun captureEvent(eventName : String, image : Sku, isSuccess : Boolean, error: String?) {
        val properties = Properties()
        properties.apply {
            this["sku_id"] = image.skuId
            this["background_id"] = image.backgroundId
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

    private fun startNextUpload(itemId: Sku) {
        com.spyneai.shoot.utils.log("next upload started")
        com.spyneai.shoot.utils.log("image to delete $itemId")
        //remove uploaded item from database
        localRepository.updateIsProcessed(itemId.projectId!!,itemId.skuId!!)

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = OneTimeWorkRequest.Builder(RecursiveProcessSkuWorker::class.java)
            .addTag("Recursive Processing Worker")

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .build())
    }
}
