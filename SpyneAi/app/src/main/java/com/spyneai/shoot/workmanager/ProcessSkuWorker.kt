package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.*
import com.spyneai.BaseApplication

class ProcessSkuWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        //start recursive process worker
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = OneTimeWorkRequest.Builder(RecursiveProcessSkuWorker::class.java)
            .addTag("Recursive Processing Worker")

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .build())

        return Result.success()
    }
}