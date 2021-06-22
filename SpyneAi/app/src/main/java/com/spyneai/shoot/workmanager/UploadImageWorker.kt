package com.spyneai.shoot.workmanager

import android.content.Context
import android.util.Log
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.base.network.ClipperApi
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.posthog.Events
import com.spyneai.service.log
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.UploadImageResponse
import com.spyneai.shoot.data.sqlite.DBHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadImageWorker(val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val localRepository = ShootLocalRepository()
    private val TAG = "UploadImageWorker"

    override suspend fun doWork(): Result {
       if (runAttemptCount > 0) return Result.failure()
        uploadImages()
        return Result.success()
    }

    private fun uploadImages() {
        try {
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


            val call = RetrofitClients.buildService(ClipperApi::class.java)
                .uploadImageInWorker(projectId,skuId,imageCategory,authKey,image)

            call.enqueue(object : Callback<UploadImageResponse> {
                override fun onResponse(
                    call: Call<UploadImageResponse>,
                    response: Response<UploadImageResponse>
                ) {
                    if (response.isSuccessful) {

                        val uploadImageResponse = response.body()

                        if (uploadImageResponse?.status == "200"){

                            captureEvent(Events.UPLOADED,true,null)
                            //update uploaded image count
                            localRepository.updateUploadCount(inputData.getString("skuId").toString())


                            // check if all image uploaded
                            if (localRepository.processSku(inputData.getString("skuId").toString())) {
                                Log.d(TAG, "onResponse: "+"process started")
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
                        }else{
                            captureEvent(Events.UPLOAD_FAILED,false,uploadImageResponse?.status)
                            uploadImages()
                        }
                    }
                }

                override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                    captureEvent(Events.UPLOAD_FAILED,false,t.localizedMessage)
                    uploadImages()
                }

            })


            //viewModel.uploadImage(projectId, skuId, imageCategory, authKey, image)


        } catch (exeption: Exception) {
            exeption.printStackTrace()
        }

    }

    private fun captureEvent(eventName : String,isSuccess : Boolean,error: String?) {
        val properties = Properties()
        properties.apply {
            this["sku_id"] = inputData.getString("projectId").toString()
            this["project_id"] = inputData.getString("projectId").toString()
            this["image_type"] = inputData.getString("projectId").toString()
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
