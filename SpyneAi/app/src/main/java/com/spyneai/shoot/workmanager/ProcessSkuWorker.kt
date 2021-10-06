package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.captureEvent
import com.spyneai.posthog.Events

class ProcessSkuWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        //check if long running worker is alive
        val workManager = WorkManager.getInstance(BaseApplication.getContext())

        val workQuery = WorkQuery.Builder
            .fromTags(listOf("Recursive Processing Worker"))
            .addStates(listOf(WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED,WorkInfo.State.RUNNING,WorkInfo.State.CANCELLED))
            .build()

        val workInfos = workManager.getWorkInfos(workQuery).await()

        if (workInfos.size > 0) {
            repeat(workInfos.size) {
                when(workInfos[it].state){
                    WorkInfo.State.BLOCKED -> {
                        BaseApplication.getContext().captureEvent(
                            Events.BLOCKED_WORKER_START_EXCEPTION,
                            Properties().putValue
                                ("name","Recursive Process Worker"))
                        start()
                    }

                    WorkInfo.State.CANCELLED -> {
                        BaseApplication.getContext().captureEvent(
                            Events.CANCELLED_WORKER_START_EXCEPTION,
                            Properties().putValue
                                ("name","Recursive Process Worker"))
                        start()
                    }
                }
            }
        } else {
            com.spyneai.shoot.utils.log("not found : start new")
            //start recursive process worker
            start()
        }

        return Result.success()
    }

    private fun start() {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

//        val longWorkRequest = OneTimeWorkRequest.Builder(RecursiveProcessSkuWorker::class.java)
//            .addTag("Recursive Processing Worker")
//
//        WorkManager.getInstance(BaseApplication.getContext())
//            .enqueue(
//                longWorkRequest
//                    .setConstraints(constraints)
//                    .build())
    }
}