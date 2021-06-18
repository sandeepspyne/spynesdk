package com.spyneai.shoot.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spyneai.base.network.ClipperApi
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.shoot.data.ProcessRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.model.UploadImageResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProcessSkuWorker (appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val processRepository = ProcessRepository()
    private val localRepository = ShootLocalRepository()
    private val TAG = "UploadImageWorker"

    override suspend fun doWork(): Result {
        if (runAttemptCount > 0) return Result.failure()
        processSku()
        return Result.success()
    }

    private suspend fun processSku() {
        try {

            val skuId = inputData.getString("skuId").toString()
            val authKey = inputData.getString("authKey").toString()
            val backgroundId = localRepository.getBackgroundId(skuId)

            //process SKU
            processRepository.processSku(authKey,skuId,backgroundId)

        } catch (e : Exception) {
            e.printStackTrace()
        }
    }
}