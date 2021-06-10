package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spyneai.shoot.data.ShootRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UploadImageWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    private val repository = ShootRepository()

    override suspend fun doWork(): Result {
        uploadImages()

        return Result.success()
    }

    private suspend fun uploadImages() {
        try {
            val uri = RequestBody.create(
                MultipartBody.FORM,
                inputData.getString("uri").toString())

            val projectId =  RequestBody.create(
                MultipartBody.FORM,
                inputData.getString("projectId").toString())
            val skuId =  RequestBody.create(
                MultipartBody.FORM,
                inputData.getString("skuId").toString())
            val imageCategory =  RequestBody.create(
                MultipartBody.FORM,
                inputData.getString("imageCategory").toString())
            val authKey =  RequestBody.create(
                MultipartBody.FORM,
                inputData.getString("authKey").toString())
            var image: MultipartBody.Part? = null
            val requestFile =
                RequestBody.create(
                    "multipart/form-data".toMediaTypeOrNull(),
                    File(uri.toString())
                )
            image =
                MultipartBody.Part.createFormData(
                    "image",
                    File(uri.toString())!!.name,
                    requestFile
                )

            repository.uploadImage(projectId, skuId, imageCategory, authKey, image)

        } catch (exeption: Exception) {
            exeption.printStackTrace()
        }

    }
}
