package com.spyneai.shoot.workmanager

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.log
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ManualUploadWorker (private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "RecursiveImageWorker"
    val localRepository = ShootLocalRepository()
    val shootRepository = ShootRepository()

    override suspend fun doWork(): Result {

        var path = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = "${Environment.DIRECTORY_DCIM}/Spyne"
        } else {
            path = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Spyne"
        }

        //get list of images
        val files = File(path).listFiles()

       // val array = inputData.getStringArray("files")
        val position = inputData.getInt("position",0)

        Log.d(TAG, "doWork: "+position+" "+ files!![position].toString())


        val longWorkRequest = OneTimeWorkRequest.Builder(ManualUploadWorker::class.java)
            .addTag("Skipped Images Long Running Worker")

        val data = Data.Builder()
           // .putStringArray("files",array)
            .putInt("position",position.plus(1))
            .build()

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setInputData(data)
                    .build())

        return Result.success()
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

    private fun captureEvent(eventName : String, image : Image, isSuccess : Boolean, error: String?) {
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

        val longWorkRequest = OneTimeWorkRequest.Builder(ManualUploadWorker::class.java)
            .addTag("Long Running Worker")

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .build())
    }
}
