package com.spyneai.shoot.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spyneai.base.network.ClipperApi
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.service.log
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

    private val repository = ShootRepository()
    private val TAG = "UploadImageWorker"

    override suspend fun doWork(): Result {
       if (runAttemptCount > 0) return Result.failure()
        uploadImages()
        return Result.success()
    }

    private suspend fun uploadImages() {
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

                    Log.d(TAG, "onResponse: "+response.body().toString())
                    var s = ""

                    if (response.isSuccessful) {
                        val uploadImageResponse = response.body()
                    }
                }

                override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                    var s = ""
                }

            })


            //viewModel.uploadImage(projectId, skuId, imageCategory, authKey, image)


        } catch (exeption: Exception) {
            exeption.printStackTrace()
        }

    }
}
