package com.spyneai.shoot.workmanager

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker

import androidx.work.Worker
import androidx.work.WorkerParameters
import com.spyneai.shoot.data.repository.ShootRepository
import com.spyneai.shoot.ui.ShootViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UploadImageWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    private val repository = ShootRepository()
    private val viewModel = ShootViewModel()

    override suspend fun doWork(): Result {
        uploadImages()

        return Result.success()
    }

    private suspend fun uploadImages() {
        try {

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
                    File(inputData.getString("capturedImage")))
            image =
                MultipartBody.Part.createFormData(
                    "image",
                    File(inputData.getString("capturedImage"))!!.name,
                    requestFile
                )

            repository.uploadImage(projectId, skuId, imageCategory, authKey, image)

        } catch (exeption: Exception) {
            exeption.printStackTrace()
        }

    }
}
