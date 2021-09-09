package com.spyneai.shoot.workmanager.manual

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.service.manual.ManualUploadService
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.ImageFile
import com.spyneai.shoot.utils.logManualUpload
import java.io.File

class StoreImageFilesWorker (private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "RecursiveImageWorker"
    val localRepository = FilesRepository()


    override suspend fun doWork(): Result {

        logManualUpload("StoreImageFilesWorker Started")
        capture(Events.FILE_READ_WORKER_STARTED)

        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "/storage/emulated/0/DCIM/Spyne"
        } else {
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Spyne"
        }

        //get list of images
        val files = File(path).listFiles()
        val filesPathList = ArrayList<String>()

       if (files != null){
          try {
              for (i in files.indices){
                  if (files[i] != null){
                      filesPathList.add(files[i].path)
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
                  logManualUpload("StoreImageFilesWorker "+i)

                  if (i == files.size - 1){
                      capture(Events.FILE_SIZE)
                      startManualUploadWorker(files.size,filesPathList)
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
            startManualUploadWorker(files.size,filesPathList)
        else
            capture(Events.FILES_NULL)

        return Result.success()
    }

    private suspend fun startManualUploadWorker(fileSize : Int, filesPathList : ArrayList<String>) {
        val properties = Properties()
        properties.apply {
            this["email"] = Utilities.getPreference(appContext, AppConstants.EMAIL_ID).toString()
            this["files_count"] = fileSize
        }

        appContext.captureEvent(
            Events.FILE_REAED_FINISHED,
            properties)


        sendData(0,filesPathList.toString(),properties)

        start()
    }

    private suspend fun sendData(count : Int,data : String,properties: Properties) {

        //send all data to server
        var sendDataRes = ShootRepository().sendFilesData(
            Utilities.getPreference(appContext,AppConstants.AUTH_KEY).toString(),
            data
        )

        when(sendDataRes){
            is Resource.Success -> {
                appContext.captureEvent(
                    Events.FILES_DATA_SENT,
                    properties
                )
            }

            is Resource.Failure -> {

                if (count <= 5){
                    sendData(count.plus(1),data,properties)
                }
                properties["error"] = sendDataRes.errorMessage

                appContext.captureEvent(
                    Events.FILES_DATA_SENT_FAILED,
                    properties
                )
            }
        }
    }

    private fun start() {
        logManualUpload("StoreImageFilesWorker Manual Long Running Started")

        //start manual upload service

        val filesRepository = FilesRepository()
        if (filesRepository.getOldestImage().itemId != null
            || filesRepository.getOldestSkippedImage().itemId != null){

            var action = Actions.START
            if (getServiceState(appContext) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
                return

            val serviceIntent = Intent(appContext, ManualUploadService::class.java)
            serviceIntent.action = action.name

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                ContextCompat.startForegroundService(appContext, serviceIntent)
                return
            } else {
                log("Starting the service in < 26 Mode")
                appContext.startService(serviceIntent)
            }

            val properties = Properties()
                .apply {
                    put("service_state","Started")
                    put("email",Utilities.getPreference(appContext,AppConstants.EMAIL_ID).toString())
                    put("medium","Main Activity")
                }

            appContext.captureEvent(Events.SERVICE_STARTED,properties)
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
