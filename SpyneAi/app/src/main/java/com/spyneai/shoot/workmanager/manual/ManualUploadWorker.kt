package com.spyneai.shoot.workmanager.manual

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
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.ImageFile
import com.spyneai.shoot.utils.logManualUpload
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ManualUploadWorker (private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "ManualImageWorker"
    val filesRepository = FilesRepository()
    val shootRepository = ShootRepository()

    override suspend fun doWork(): Result {
        capture(Events.MANUAL_UPLAOD_STRATED)

        val image = filesRepository.getOldestImage()

        if (runAttemptCount > 4) {
            if (image.itemId != null){
                logManualUpload("Manual upload skipped")
                filesRepository.skipImage(image.itemId!!,-1)
                startNextUpload(image.itemId!!,false)
            }

           captureEvent(Events.MANUAL_UPLOAD_FAILED,image,false,"Image upload limit reached")

            return Result.failure()
        }

        if (image.itemId != null){
            if (image.imagePath != null){
                if (!File(image.imagePath!!).exists()){
                    filesRepository.deleteImage(image.itemId!!)
                    captureEvent(Events.MANUAL_UPLOAD_FAILED,image,false,"Image file got deleted by user")
                    return Result.failure()
                }
            }

            val authKey =
                Utilities.getPreference(appContext, AppConstants.AUTH_KEY).toString()

            //check upload status
            var uploadStatuRes = shootRepository.checkUploadStatus(
                authKey,
                File(image.imagePath).name,
            )

            when(uploadStatuRes) {
                is Resource.Success -> {

                    logManualUpload("Check Status success "+uploadStatuRes.value.data.upload)
                    image.projectId = uploadStatuRes.value.data.projectId
                    captureEvent(Events.CHECK_UPLOAD_STATUS,image,true,null)

                    if (uploadStatuRes.value.data.upload){
                        //start next image
                        captureEvent(Events.ALREADY_UPLOAD_STATUS,image,true,null)
                        startNextUpload(image.itemId!!,true)
                        return Result.success()
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
                            uploadStatuRes.value.data.skuId.toRequestBody(MultipartBody.FORM),
                            uploadStatuRes.value.data.imageCategory.toRequestBody(MultipartBody.FORM),
                            authKey.toRequestBody(MultipartBody.FORM),
                            "Retry".toRequestBody(MultipartBody.FORM),
                            uploadStatuRes.value.data.sequence,
                            imageFile)

                        when(response){
                            is Resource.Success -> {
                                logManualUpload("Manual upload success")
                                captureEvent(Events.MANUALLY_UPLOADED,image,true,null)
                                startNextUpload(image.itemId!!,true)
                                return Result.success()
                            }

                            is Resource.Failure -> {
                                logManualUpload("Manual upload failed")
                                if(response.errorMessage == null){
                                    captureEvent(Events.MANUAL_UPLOAD_FAILED,image,false,response.errorCode.toString()+": Http exception from server")
                                }else {
                                    captureEvent(Events.MANUAL_UPLOAD_FAILED,image,false,response.errorCode.toString()+": "+response.errorMessage)
                                }
                                return Result.retry()
                            }
                        }

                    }
                }

                is Resource.Failure -> {
                    logManualUpload("Check Status failed "+ uploadStatuRes.errorMessage)
                    if(uploadStatuRes.errorMessage == null){
                        captureEvent(Events.CHECK_UPLOAD_STATUS_FAILED,image,false,uploadStatuRes.errorCode.toString()+": Http exception from server")
                    }else {
                        captureEvent(Events.CHECK_UPLOAD_STATUS_FAILED,image,false,uploadStatuRes.errorCode.toString()+": "+uploadStatuRes.errorMessage)
                    }
                    return Result.retry()
                }
            }

            com.spyneai.shoot.utils.log("image selected "+image.itemId + " "+image.imagePath)
            return Result.success()
        }else{
            Log.d(TAG, "doWork: start skip worker")
            //start skipped images worker
            startSkippedImagesWorker()

            return Result.success()
        }
    }

    private fun startSkippedImagesWorker() {
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

    private fun captureEvent(eventName : String,image : ImageFile,isSuccess : Boolean,error: String?) {
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
            filesRepository.deleteImage(itemId)

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = OneTimeWorkRequest.Builder(ManualUploadWorker::class.java)
            .addTag("Manual Long Running Worker")

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
