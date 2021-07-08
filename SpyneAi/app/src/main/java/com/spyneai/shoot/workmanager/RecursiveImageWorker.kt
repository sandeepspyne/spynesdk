package com.spyneai.shoot.workmanager

import android.content.Context
import android.util.Log
import androidx.concurrent.callback.CallbackToFutureAdapter
import androidx.work.*
import androidx.work.ListenableWorker.Result.*
import com.google.common.util.concurrent.ListenableFuture
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
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
import retrofit2.Response
import java.io.File
import java.io.IOException


class RecursiveImageWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "RecursiveImageWorker"
    val localRepository = ShootLocalRepository()
    val shootRepository = ShootRepository()

    override suspend fun doWork(): Result {

        if (runAttemptCount > 4) {
            return failure()
        }

        val image = localRepository.getOldestImage()

        if (image.itemId != null){
            var jobs : Deferred<Resource<UploadImageResponse>>?

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

            imageFile =
                MultipartBody.Part.createFormData(
                    "image",
                    File(image.imagePath)!!.name,
                    requestFile
                )

            coroutineScope {
                jobs =
                    async {
                        shootRepository.uploadImage(projectId!!,
                            skuId!!, imageCategory!!,authKey,imageFile)
                    }

                jobs!!.await()
            }

            return if (jobs?.getCompleted() is Resource.Success) {
                com.spyneai.shoot.utils.log("upload image sucess")
                captureEvent(Events.UPLOADED,image,true,null)
                startNextUpload(image.itemId!!)
                success()
            }else{
                com.spyneai.shoot.utils.log("Upload image failed")
                val error = jobs?.getCompletionExceptionOrNull()?.localizedMessage

                captureEvent(Events.UPLOAD_FAILED,image,false,error)
                retry()
            }
        }else{
            return success()
        }
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

    private fun startNextUpload(itemId: Long) {
        com.spyneai.shoot.utils.log("next upload started")
        com.spyneai.shoot.utils.log("image to delete $itemId")
        //remove uploaded item from database
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
