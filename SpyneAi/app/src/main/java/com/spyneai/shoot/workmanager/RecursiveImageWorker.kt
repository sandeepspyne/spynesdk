package com.spyneai.shoot.workmanager

import android.content.Context
import android.os.Handler
import android.util.Log

import androidx.work.*
import androidx.work.ListenableWorker.Result.*
import com.google.common.util.concurrent.ListenableFuture
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.base.network.ServerException
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.data.model.UploadImageResponse
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.wait
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException


class RecursiveImageWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "RecursiveImageWorker"
    val localRepository = ShootLocalRepository()
    val shootRepository = ShootRepository()

    override suspend fun doWork(): Result {

        Log.d(TAG, "doWork: start "+runAttemptCount)

        val image = localRepository.getOldestImage()

        if (runAttemptCount > 4) {
            if (image.itemId != null){
                localRepository.skipImage(image.itemId!!,-1)
                startNextUpload(image.itemId!!,false)
            }

            Log.d(TAG, "doWork: failure limit")
            captureEvent(Events.UPLOAD_FAILED,image,false,"Image upload limit reached")

            return failure()
        }

        if (image.itemId != null){
            if (image.imagePath != null){
                if (!File(image.imagePath!!).exists()){
                    localRepository.deleteImage(image.itemId!!)
                    captureEvent(Events.UPLOAD_FAILED,image,false,"Image file got deleted by user")
                    return failure()
                }
            }

            com.spyneai.shoot.utils.log("image selected "+image.itemId + " "+image.imagePath)

            val projectId = image.projectId?.toRequestBody(MultipartBody.FORM)

            val skuId = image.skuId?.toRequestBody(MultipartBody.FORM)
            val imageCategory =
                image.categoryName?.toRequestBody(MultipartBody.FORM)

            val authKey =
                Utilities.getPreference(appContext,AppConstants.AUTH_KEY).toString().toRequestBody(MultipartBody.FORM)

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
                skuId!!, imageCategory!!,authKey, image.sequence!!,imageFile)

            when(response){
                is Resource.Success -> {
                    Log.d(TAG, "doWork: success")
                    captureEvent(Events.UPLOADED,image,true,null)
                    startNextUpload(image.itemId!!,true)
                    return success()
                }

                is Resource.Failure -> {
                    Log.d(TAG, "doWork: failure")
                    if(response.errorMessage == null){
                        captureEvent(Events.UPLOAD_FAILED,image,false,response.errorCode.toString()+": Http exception from server")
                    }else {
                        captureEvent(Events.UPLOAD_FAILED,image,false,response.errorCode.toString()+": "+response.errorMessage)
                    }
                    return retry()
                }
            }

            return success()
        }else{
            Log.d(TAG, "doWork: start skip worker")
            //start skipped images worker
                startSkippedImagesWorker()

            return success()
        }
    }

    private fun startSkippedImagesWorker() {
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

    private fun captureEvent(eventName : String,image : Image,isSuccess : Boolean,error: String?) {
        val properties = Properties()
        properties.apply {
            this["sku_id"] = image.skuId
            this["project_id"] = image.projectId
            this["image_type"] = image.categoryName
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

            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val longWorkRequest = OneTimeWorkRequest.Builder(RecursiveImageWorker::class.java)
                .addTag("Long Running Worker")

            WorkManager.getInstance(BaseApplication.getContext())
                .enqueue(
                    longWorkRequest
                        .setConstraints(constraints)
                        .build())
    }
}
