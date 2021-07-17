package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.base.network.Resource
import com.spyneai.base.network.ServerException
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class UploadImageWorker(val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val localRepository = ShootLocalRepository()
    val shootRepository = ShootRepository()
    private val TAG = "UploadImageWorker"

    override suspend fun doWork(): Result {

        if (runAttemptCount > 8) {
            captureEvent(Events.UPLOAD_FAILED,false,"Image upload limit  reached")
            return Result.failure()
        }

        val projectId =
            inputData.getString("projectId").toString().toRequestBody(MultipartBody.FORM)
        val skuId = inputData.getString("skuId").toString().toRequestBody(MultipartBody.FORM)
        val imageCategory =
            inputData.getString("imageCategory").toString().toRequestBody(MultipartBody.FORM)
        val authKey =
            inputData.getString("authKey").toString().toRequestBody(MultipartBody.FORM)

        var image: MultipartBody.Part? = null
        val requestFile =
            File(inputData.getString("uri")).asRequestBody("multipart/form-data".toMediaTypeOrNull())
        image =
            MultipartBody.Part.createFormData(
                "image",
                File(inputData.getString("uri"))!!.name,
                requestFile
            )

        var jobs : Deferred<Resource<Any>>?

        coroutineScope {
            jobs =
                async {
                    shootRepository.uploadImage(projectId!!,
                        skuId!!, imageCategory!!,authKey, inputData.getInt("sequence",0),image)
                }

            jobs!!.await()
        }

        return if (jobs?.getCompleted() is Resource.Success) {
            com.spyneai.shoot.utils.log("upload image success")
            captureEvent(Events.UPLOADED,true,null)

            localRepository.updateUploadCount(inputData.getString("skuId").toString())

            // check if all image uploaded
            if (inputData.getBoolean("processSku",true)
                &&
                localRepository.processSku(inputData.getString("skuId").toString())) {
                //process sku
                val processSkuWorkRequest =
                    OneTimeWorkRequest.Builder(ProcessSkuWorker::class.java)

                val data = Data.Builder()
                data.putString("skuId", inputData.getString("skuId").toString())
                data.putString("authKey", inputData.getString("authKey").toString())

                processSkuWorkRequest.setInputData(data.build())
                WorkManager.getInstance(applicationContext)
                    .enqueue(processSkuWorkRequest.build())
            }

            Result.success()
        }else{

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

            com.spyneai.shoot.utils.log("Upload image failed $error")

            captureEvent(Events.UPLOAD_FAILED,false,error)
            Result.retry()
        }
    }


    private fun captureEvent(eventName : String,isSuccess : Boolean,error: String?) {
        val properties = Properties()
        properties.apply {
            this["sku_id"] = inputData.getString("skuId").toString()
            this["project_id"] = inputData.getString("projectId").toString()
            this["image_type"] = inputData.getString("imageCategory").toString()
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
