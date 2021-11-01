package com.spyneai.service.manual

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.posthog.android.Properties
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.ImageFile
import com.spyneai.shoot.utils.logManualUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File

class StoreImageFiles(val appContext: Context,
                      val shootRepository: ShootRepository,
                      val localRepository: FilesRepository) {

    suspend fun startWork() {
        logManualUpload("StoreImageFilesWorker Started")
        capture(Events.FILE_READ_WORKER_STARTED)

       GlobalScope.launch(Dispatchers.Default){
           val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               "/storage/emulated/0/DCIM/Spyne"
           } else {
               "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Spyne"
           }

           //get list of images
           val files = File(path).listFiles()
           val filesPathList = JSONArray()

           if (files != null){
               try {
                   for (i in files.indices){
                       if (files[i] != null){
                           filesPathList.put(files[i].path.toString())
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
                           return@launch
                       }
                   }
               }catch (e : Exception){
                   val properties = HashMap<String,Any?>()
                   properties.put("error",e.localizedMessage)

                   appContext.captureEvent("FileNameError",properties)
               }
           }

           if (files != null)
               startManualUploadWorker(files.size,filesPathList)
           else
               capture(Events.FILES_NULL)
       }
    }


    private suspend fun startManualUploadWorker(fileSize : Int, filesPathList : JSONArray) {
        val properties = HashMap<String,Any?>()
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

    private suspend fun sendData(count : Int,data : String,properties: HashMap<String,Any?>) {

        //send all data to server
        var sendDataRes = ShootRepository().sendFilesData(
            Utilities.getPreference(appContext, AppConstants.AUTH_KEY).toString(),
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

            Utilities.savePrefrence(appContext, AppConstants.START_FILES_WORKER,"Files Worker Finished")


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                ContextCompat.startForegroundService(appContext, serviceIntent)
                return
            } else {
                log("Starting the service in < 26 Mode")
                appContext.startService(serviceIntent)
            }
        }
    }

    private fun capture(eventName : String) {
        val properties = HashMap<String,Any?>()
        properties.apply {
            this["email"] = Utilities.getPreference(appContext, AppConstants.EMAIL_ID).toString()
        }

        appContext.captureEvent(
            eventName,
            properties)
    }
}