package com.spyneai.shoot.workmanager.manual

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.work.*
import com.spyneai.BaseApplication
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.model.ImageFile
import java.io.File

class StoreImageFilesWorker (private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "RecursiveImageWorker"
    val localRepository = FilesRepository()


    override suspend fun doWork(): Result {
        var path = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = "${Environment.DIRECTORY_DCIM}/Spyne"
        } else {
            path = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Spyne"
        }

        //get list of images
        val files = File(path).listFiles()

       if (files != null){
           for (i in files.indices){
               if (files[i] != null){
                   val fileName = files[i].name

                   val properties =  fileName.split("_")

                   if (properties.size == 4){
                       val imageFile = ImageFile()
                       imageFile.skuName = properties[0]
                       imageFile.skuId = properties[1]
                       imageFile.categoryName = properties[2]
                       imageFile.sequence = properties[3].substringBefore(".").toInt()
                       imageFile.imagePath = files[i].path
                       localRepository.insertImageFile(imageFile)
                   }
               }
               Log.d(TAG, "doWork: "+i)

               if (i == files.size - 1){
                   startManualUploadWorker()
                   return Result.success()
               }

           }
       }

        if (files != null)
        startManualUploadWorker()
        return Result.success()
    }

    private suspend fun startManualUploadWorker() {
        //check if long running worker is alive
        val workManager = WorkManager.getInstance(BaseApplication.getContext())

        val workQuery = WorkQuery.Builder
            .fromTags(listOf("Manual Long Running Worker"))
            .addStates(listOf(WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING))
            .build()

        val workInfos = workManager.getWorkInfos(workQuery).await()

        Log.d(TAG, "insertImage: "+workInfos.size)

        if (workInfos.size > 0) {
            com.spyneai.shoot.utils.log("alive : ")
        } else {
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
}
