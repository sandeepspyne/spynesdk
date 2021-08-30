package com.spyneai.shoot.workmanager.manual

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
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.ImageFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ManualSkippedImageWorker (private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    val TAG = "ManualSkippedImageWorker"
    val fileRepository = FilesRepository()
    val shootRepository = ShootRepository()

    override suspend fun doWork(): Result {

        capture(Events.MANUAL_SKIPPED_UPLAOD_STRATED)

        val image = fileRepository.getOldestSkippedImage()

        if (runAttemptCount > 4) {
            //skip with value -2 to move worker on next image
            if (image.itemId != null){
                fileRepository.skipImage(image.itemId!!,-2)
                startNextUpload(image.itemId!!,false)
            }

            captureEvent(Events.MANUAL_SKIPPED_UPLOAD_FAILED,image,false,"Image upload limit  reached")
            return Result.failure()
        }


        if (image.itemId != null){

            if (image.imagePath != null){
                if (!File(image.imagePath!!).exists()){
                    fileRepository.deleteImage(image.itemId!!)
                    captureEvent(Events.MANUAL_SKIPPED_UPLOAD_FAILED,image,false,"Image file got deleted by user")
                    return Result.failure()
                }
            }

            com.spyneai.shoot.utils.log("image selected "+image.itemId + " "+image.imagePath)

            val authKey =
                Utilities.getPreference(appContext, AppConstants.AUTH_KEY).toString()

            //check upload status
            var uploadStatuRes = shootRepository.checkUploadStatus(
                authKey,
                image.skuId!!,
                image.categoryName!!,
                image.sequence!!
            )

            when(uploadStatuRes) {
                is Resource.Success -> {
                    image.projectId = uploadStatuRes.value.data.projectId
                    captureEvent(Events.CHECK_UPLOAD_STATUS,image,true,null)

                    if (uploadStatuRes.value.data.upload){
                        //start next image
                        captureEvent(Events.ALREADY_UPLOAD_STATUS,image,true,null)
                        startNextUpload(image.itemId!!,true)
                    }else {
                        captureEvent(Events.ALREADY_NOT_UPLOAD_STATUS,image,true,null)
                        //make upload call
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

                        var response = shootRepository.uploadImage(
                            uploadStatuRes.value.data.projectId.toRequestBody(MultipartBody.FORM),
                            image.skuId!!.toRequestBody(MultipartBody.FORM),
                            image.categoryName!!.toRequestBody(MultipartBody.FORM),
                            authKey.toRequestBody(MultipartBody.FORM),
                            "Retry".toRequestBody(MultipartBody.FORM),
                            image.sequence!!,
                            imageFile)

                        when(response){
                            is Resource.Success -> {

                                captureEvent(Events.MANUAL_SKIPED_UPLOADED,image,true,null)
                                startNextUpload(image.itemId!!,true)
                                return Result.success()
                            }

                            is Resource.Failure -> {

                                if(response.errorMessage == null){
                                    captureEvent(Events.MANUAL_SKIPPED_UPLOAD_FAILED,image,false,response.errorCode.toString()+": Http exception from server")
                                }else {
                                    captureEvent(Events.MANUAL_SKIPPED_UPLOAD_FAILED,image,false,response.errorCode.toString()+": "+response.errorMessage)
                                }
                                return Result.retry()
                            }
                        }

                    }
                }

                is Resource.Failure -> {
                    if(uploadStatuRes.errorMessage == null){
                        captureEvent(Events.CHECK_UPLOAD_STATUS_FAILED,image,false,uploadStatuRes.errorCode.toString()+": Http exception from server")
                    }else {
                        captureEvent(Events.CHECK_UPLOAD_STATUS_FAILED,image,false,uploadStatuRes.errorCode.toString()+": "+uploadStatuRes.errorMessage)
                    }
                    return Result.retry()
                }
            }

            return Result.success()
        }else{
            //update status of skipped images -2 to -1 to retry again
            val count = fileRepository.updateSkipedImages()

            if (count > 0)
                startWorker()

            return Result.success()
        }
    }

    private fun captureEvent(eventName : String, image : ImageFile, isSuccess : Boolean, error: String?) {
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
        //remove uploaded item from database
        if (uploaded)
            fileRepository.deleteImage(itemId)

        startWorker()
    }

    private fun startWorker() {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = OneTimeWorkRequest.Builder(ManualSkippedImageWorker::class.java)
            .addTag("Manual Skipped Images Long Running Worker")

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .build())
    }

    private fun capture(eventName : String) {
        val properties = Properties()
        properties.apply {
            this["email"] = Utilities.getPreference(appContext, AppConstants.EMAIL_ID).toString()
            this["this[\"retry_count\"] = runAttemptCount"] = runAttemptCount
        }

        appContext.captureEvent(
            eventName,
            properties)
    }
}