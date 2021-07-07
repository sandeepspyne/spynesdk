package com.spyneai.shoot.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.spyneai.shoot.data.ShootLocalRepository

class LongRunningWorker(val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    val TAG = "LongRunningWorker"

    override suspend fun doWork(): Result {

        val localRepository = ShootLocalRepository()

        val image = localRepository.getOldestImage()

        if (image.itemId != null){
            Log.d(TAG, "doWork: "+image.skuId)
            Log.d(TAG, "doWork: "+image.imagePath)


        }else{
            Log.d(TAG, "doWork: "+" Image null ")
        }


        return Result.success()
    }

}