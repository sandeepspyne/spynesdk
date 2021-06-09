package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.CoroutineWorker

import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadImageWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        uploadImages()
        return Result.success()
    }

    private fun uploadImages() {
        try {

        } catch (exeption: Exception) {
            exeption.printStackTrace()
        }

    }
}