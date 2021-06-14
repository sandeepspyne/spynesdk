package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spyneai.shoot.data.repository.ShootRepository
import com.spyneai.shoot.ui.ShootViewModel
import kotlinx.coroutines.flow.collect
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UploadImageWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val repository = ShootRepository()
    private val viewModel = ShootViewModel()

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
                File(inputData.getString("capturedImage")).asRequestBody("multipart/form-data".toMediaTypeOrNull())
            image =
                MultipartBody.Part.createFormData(
                    "image",
                    File(inputData.getString("capturedImage"))!!.name,
                    requestFile
                )
            viewModel.uploadImage(projectId, skuId, imageCategory, authKey, image)

        } catch (exeption: Exception) {
            exeption.printStackTrace()
        }

    }
}
