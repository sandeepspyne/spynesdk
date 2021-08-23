package com.spyneai.shoot.workmanager.manual

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.work.*
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

            if (i == files.size - 1)
                return Result.success()
        }

        return Result.success()

    }
}
