package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.captureEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import java.util.concurrent.TimeUnit

class ParentRecursiveWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        //check if long running worker is alive
        val workManager = WorkManager.getInstance(BaseApplication.getContext())

        val workQuery = WorkQuery.Builder
            .fromTags(listOf("Long Running Worker"))
            .addStates(listOf(
                WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
                WorkInfo.State.CANCELLED))
            .build()

        val workInfos = workManager.getWorkInfos(workQuery).await()

        // Log.d(TAG, "insertImage: "+workInfos.size)

        val properties = Properties()

        properties.apply {
            this["email"] = Utilities.getPreference(BaseApplication.getContext(), AppConstants.EMAIL_ID).toString()
        }

        if (workInfos.size > 0) {
            com.spyneai.shoot.utils.log("alive : ")
            var s = ""
            try {
                repeat(workInfos.size) {
                    when(workInfos[it].state){
                        WorkInfo.State.BLOCKED -> {
                            BaseApplication.getContext().captureEvent(
                                Events.BLOCKED_WORKER_START_EXCEPTION,
                                Properties().putValue
                                    ("name","Recursive Upload"))
                            startLongRunningWorker()
                        }

                        WorkInfo.State.CANCELLED -> {
                            BaseApplication.getContext().captureEvent(
                                Events.CANCELLED_WORKER_START_EXCEPTION,
                                Properties().putValue
                                    ("name","Recursive Upload"))
                            startLongRunningWorker()
                        }
                        else -> {
                            var hi = "hi"
                        }
                    }
                }

            }catch (e : Exception){
                var hi = "hi"

            }

            BaseApplication.getContext().captureEvent(
                Events.RECURSIVE_UPLOAD_ALREADY_RUNNING,
                properties)

        } else {
            com.spyneai.shoot.utils.log("not found : start new")
            //start long running worker
            BaseApplication.getContext().captureEvent(
                Events.RECURSIVE_UPLOAD_INTIATED,
                properties)
            startLongRunningWorker()
        }

        return Result.success()
    }

    fun startLongRunningWorker() {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = OneTimeWorkRequest.Builder(RecursiveImageWorker::class.java)
            .addTag("Long Running Worker")
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS)

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .build())
    }
}