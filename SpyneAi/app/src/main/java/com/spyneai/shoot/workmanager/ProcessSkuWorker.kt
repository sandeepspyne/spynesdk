package com.spyneai.shoot.workmanager

import android.content.Context
import android.util.Log
import androidx.work.*
import com.spyneai.BaseApplication

class ProcessSkuWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        //check if long running worker is alive
        val workManager = WorkManager.getInstance(BaseApplication.getContext())

        val workQuery = WorkQuery.Builder
            .fromTags(listOf("Recursive Processing Worker"))
            .addStates(listOf(WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED,WorkInfo.State.RUNNING))
            .build()

        val workInfos = workManager.getWorkInfos(workQuery).await()

        if (workInfos.size > 0) {
            com.spyneai.shoot.utils.log("alive : ")
        } else {
            com.spyneai.shoot.utils.log("not found : start new")
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
        }

        return Result.success()
    }
}