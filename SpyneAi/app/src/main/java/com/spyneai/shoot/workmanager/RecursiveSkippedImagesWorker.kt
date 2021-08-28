package com.spyneai.shoot.workmanager

import android.content.Context
import android.util.Log
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RecursiveSkippedImagesWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "RecursiveSkippedImagesWorker"
    val localRepository = ShootLocalRepository()
    val shootRepository = ShootRepository()

    override suspend fun doWork(): Result {

        capture(Events.RECURSIVE_SKIPPED_UPLAOD_STRATED,runAttemptCount)

        val image = localRepository.getOldestSkippedImage()

        if (runAttemptCount > 4) {
            //skip with value -2 to move worker on next image
            if (image.itemId != null){
                localRepository.skipImage(image.itemId!!,-2)
                startNextUpload(image.itemId!!,false)
            }


            captureEvent(Events.SKIPPED_UPLOAD_FAILED,image,false,"Image upload limit  reached")
            return Result.failure()
        }



        if (image.itemId != null){

            if (image.imagePath != null){
                if (!File(image.imagePath!!).exists()){
                    localRepository.deleteImage(image.itemId!!)
                    captureEvent(Events.SKIPPED_UPLOAD_FAILED,image,false,"Image file got deleted by user")
                    return Result.failure()
                }
            }

            com.spyneai.shoot.utils.log("image selected "+image.itemId + " "+image.imagePath)

            val projectId = image.projectId?.toRequestBody(MultipartBody.FORM)

            val skuId = image.skuId?.toRequestBody(MultipartBody.FORM)
            val imageCategory =
                image.categoryName?.toRequestBody(MultipartBody.FORM)

            val authKey =
                Utilities.getPreference(appContext, AppConstants.AUTH_KEY).toString().toRequestBody(
                    MultipartBody.FORM)

            var imageFile: MultipartBody.Part? = null
            val requestFile =
                File(image.imagePath).asRequestBody("multipart/form-data".toMediaTypeOrNull())

            val fileName = if (image.categoryName == "360int") {
                image.skuName + "_" + image.skuId + "_360int_1"
            }else {
                File(image.imagePath)!!.name
            }

            imageFile =
                MultipartBody.Part.createFormData(
                    "image",
                    fileName,
                    requestFile
                )

            var response = shootRepository.uploadImage(projectId!!,
                skuId!!, imageCategory!!,authKey, "Retry".toRequestBody(MultipartBody.FORM),image.sequence.toString(),imageFile)

            when(response){
                is Resource.Success -> {
                    captureEvent(Events.SKIPED_UPLOADED,image,true,null)
                    startNextUpload(image.itemId!!,true)
                    return Result.success()
                }

                is Resource.Failure -> {
                    if(response.errorMessage == null){
                        captureEvent(Events.SKIPPED_UPLOAD_FAILED,image,false,response.errorCode.toString()+": Http exception from server")
                    }else {
                        captureEvent(Events.SKIPPED_UPLOAD_FAILED,image,false,response.errorCode.toString()+": "+response.errorMessage)
                    }
                    return Result.retry()
                }
            }

            return Result.success()
        }else{
            //update status of skipped images -2 to -1 to retry again
            val count = localRepository.updateSkipedImages()

            if (count > 0)
                startWorker()

            return Result.success()
        }
    }

    private fun captureEvent(eventName : String, image : Image, isSuccess : Boolean, error: String?) {
        val properties = Properties()
        properties.apply {
            this["sku_id"] = image.skuId
            this["project_id"] = image.projectId
            this["image_type"] = image.categoryName
            this["sequence"] = image.sequence
            this["retry_count"] = runAttemptCount
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

    private fun startNextUpload(itemId: Long,uploaded : Boolean) {
        com.spyneai.shoot.utils.log("next upload started")
        com.spyneai.shoot.utils.log("image to delete $itemId")
        //remove uploaded item from database
        if (uploaded)
            localRepository.deleteImage(itemId)

        startWorker()
    }

    private fun startWorker() {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = OneTimeWorkRequest.Builder(RecursiveSkippedImagesWorker::class.java)
            .addTag("Skipped Images Long Running Worker")

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .build())
    }

    private fun capture(eventName : String,retryAttempt : Int) {
        val properties = Properties()
        properties.apply {
            this["email"] = Utilities.getPreference(appContext, AppConstants.EMAIL_ID).toString()
            this["retry_count"] = retryAttempt
        }

        appContext.captureEvent(
            eventName,
            properties)
    }
}