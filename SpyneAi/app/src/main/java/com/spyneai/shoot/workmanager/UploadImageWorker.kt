package com.spyneai.shoot.workmanager

import android.content.Context
import android.util.Log
import androidx.work.*
import com.spyneai.base.network.ClipperApi
import com.spyneai.interfaces.RetrofitClients
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

class UploadImageWorker(appContext: Context, workerParams: WorkerParameters) :
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

                    Log.d(TAG, "onResponse: " + response.body().toString())

                    if (response.isSuccessful) {
                        val uploadImageResponse = response.body()

                        //update uploaded image count
                        localRepository.updateUploadCount(inputData.getString("skuId").toString())

                        // check if all image uploaded
                        if (localRepository.processSku(inputData.getString("skuId").toString())) {
                            //process sku
                            val processSkuWorkRequest = OneTimeWorkRequest.Builder(ProcessSkuWorker::class.java)

                            val data = Data.Builder()
                            data.putString("skuId", inputData.getString("skuId").toString())
                            data.putString("authKey",  inputData.getString("authKey").toString())

                            processSkuWorkRequest.setInputData(data.build())
                            WorkManager.getInstance(applicationContext).enqueue(processSkuWorkRequest.build())

                        }
                    }
                }

                override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                    Log.d(TAG, "onFailure: "+t.localizedMessage)

                }

            })


            //viewModel.uploadImage(projectId, skuId, imageCategory, authKey, image)


        } catch (exeption: Exception) {
            exeption.printStackTrace()
        }

    }
}
