package com.spyneai.shoot.workmanager.manual

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.captureEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.model.ImageFile
import java.io.File

class StoreImageFilesWorker (private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "RecursiveImageWorker"
    val localRepository = FilesRepository()


    override suspend fun doWork(): Result {

        capture(Events.FILE_READ_WORKER_STARTED)

        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "/storage/emulated/0/DCIM/Spyne"
        } else {
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Spyne"
        }

        //get list of images
        val files = File(path).listFiles()

       if (files != null){
          try {
              for (i in files.indices){
                  if (files[i] != null){
                      val fileName = files[i].name

                      val properties =  fileName.split("_")

                      if (properties.size == 4){
                          val imageFile = ImageFile()
                          imageFile.skuName = properties[0]
                          imageFile.skuId = properties[1]
                          imageFile.categoryName = properties[2]
                          imageFile.sequence = properties[3].substringBefore(".")
                          imageFile.imagePath = files[i].path
                          localRepository.insertImageFile(imageFile)
                      }
                  }
                  Log.d(TAG, "doWork: "+i)

                  if (i == files.size - 1){
                      capture(Events.FILE_SIZE)
                      startManualUploadWorker(files.size)
                      return Result.success()
                  }

              }
          }catch (e : Exception){
              val properties = Properties()
              properties.put("error",e.localizedMessage)

              appContext.captureEvent("FileNameError",properties)
          }
       }

        if (files != null)
            startManualUploadWorker(files.size)
        else
            capture(Events.FILES_NULL)

        return Result.success()
    }

    private suspend fun startManualUploadWorker(fileSize : Int) {
        val properties = Properties()
        properties.apply {
            this["email"] = Utilities.getPreference(appContext, AppConstants.EMAIL_ID).toString()
            this["files_count"] = fileSize
        }

        appContext.captureEvent(
            Events.FILE_REAED_FINISHED,
            properties)

        //check if long running worker is alive
        val workManager = WorkManager.getInstance(BaseApplication.getContext())

        val workQuery = WorkQuery.Builder
            .fromTags(listOf("Manual Long Running Worker"))
            .addStates(listOf(WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING,WorkInfo.State.CANCELLED))
            .build()

        val workInfos = workManager.getWorkInfos(workQuery).await()

        Log.d(TAG, "insertImage: "+workInfos.size)

        if (workInfos.size > 0) {
            capture(Events.MANUAL_WORKER_ALREADY_RUNNING)
            com.spyneai.shoot.utils.log("alive : ")
        } else {
            capture(Events.MANUAL_WORKER_INITIATED)
            com.spyneai.shoot.utils.log("not found : start new")
            //start long running worker
            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val longWorkRequest = OneTimeWorkRequest.Builder(ManualUploadWorker::class.java)
                .addTag("Manual Long Running Worker")

            WorkManager.getInstance(BaseApplication.getContext())
                .enqueue(
                    longWorkRequest
                        .setConstraints(constraints)
                        .build())
        }
    }

    private fun capture(eventName : String) {
        val properties = Properties()
        properties.apply {
            this["email"] = Utilities.getPreference(appContext, AppConstants.EMAIL_ID).toString()
            this["retry_count"] = runAttemptCount
        }

        appContext.captureEvent(
            eventName,
            properties)
    }
}
