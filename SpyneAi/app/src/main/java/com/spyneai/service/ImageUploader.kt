package com.spyneai.service

import android.content.Context
import android.util.Log
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.isInternetActive
import com.spyneai.model.processImageService.Task
import com.spyneai.model.upload.UploadResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.utils.logUpload
import com.spyneai.shoot.workmanager.RecursiveImageWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.TimeUnit

class ImageUploader(val context: Context,
                    val localRepository : ShootLocalRepository,
                    val shootRepository: ShootRepository,
                    var listener: Listener) {


    fun start() {
        selectLastImageAndUpload()
    }

    private fun selectLastImageAndUpload() {

       if (context.isInternetActive()){
           GlobalScope.launch(Dispatchers.Default) {

               val image = localRepository.getOldestImage()

               //uploading enqueued
               listener.inProgress(image)

               if (image.itemId != null){
                   logUpload("Upload Started "+image.itemId)

                   if (image.imagePath != null){
                       if (!File(image.imagePath!!).exists()){
                           localRepository.deleteImage(image.itemId!!)
                           captureEvent(Events.UPLOAD_FAILED,image,false,"Image file got deleted by user")
//                        return ListenableWorker.Result.failure()
                       }
                   }

                   val projectId = image.projectId?.toRequestBody(MultipartBody.FORM)

                   val skuId = image.skuId?.toRequestBody(MultipartBody.FORM)
                   val imageCategory =
                       image.categoryName?.toRequestBody(MultipartBody.FORM)

                   val authKey =
                       Utilities.getPreference(context, AppConstants.AUTH_KEY).toString().toRequestBody(MultipartBody.FORM)

                   var imageFile: MultipartBody.Part? = null
                   val requestFile =
                       File(image.imagePath).asRequestBody("multipart/form-data".toMediaTypeOrNull())

                   val fileName = if (image.categoryName == "360int") {
                       image.skuName + "_" + image.skuId + "_360int_1"
                   }else {
                       File(image.imagePath)!!.name
                   }

                   com.spyneai.shoot.utils.log("Image Name " + fileName)

                   imageFile =
                       MultipartBody.Part.createFormData(
                           "image",
                           fileName,
                           requestFile
                       )

                   // val uploadType = if (runAttemptCount == 0) "Direct" else "Retry"

                   var response = shootRepository.uploadImage(projectId!!,
                       skuId!!, imageCategory!!,authKey, "Direct".toRequestBody(MultipartBody.FORM),image.sequence!!,imageFile)

                   when(response){
                       is Resource.Success -> {
                           captureEvent(Events.UPLOADED,image,true,null)
                           startNextUpload(image.itemId!!,true)
                       }

                       is Resource.Failure -> {
                          logUpload("Upload error "+response.errorCode.toString()+" "+response.errorMessage)
                           if(response.errorMessage == null){
                               captureEvent(Events.UPLOAD_FAILED,image,false,response.errorCode.toString()+": Http exception from server")
                           }else {
                               captureEvent(Events.UPLOAD_FAILED,image,false,response.errorCode.toString()+": "+response.errorMessage)
                           }
                           selectLastImageAndUpload()
                       }
                   }

               }else{
                   logUpload("All Images uploaded")
                   //start skipped images worker
                   // startSkippedImagesWorker()

                   listener.onUploaded(image)
               }
           }
       }else {
           listener.onConnectionLost()
       }
    }

    private fun startNextUpload(itemId: Long,uploaded : Boolean) {
       logUpload("Start next upload "+uploaded)
        //remove uploaded item from database
        if (uploaded)
            localRepository.deleteImage(itemId)

        selectLastImageAndUpload()
    }

    private fun captureEvent(eventName : String, image : Image, isSuccess : Boolean, error: String?) {
        val properties = Properties()
        properties.apply {
            this["sku_id"] = image.skuId
            this["project_id"] = image.projectId
            this["image_type"] = image.categoryName
            this["sequence"] = image.sequence
           // this["retry_count"] = runAttemptCount
        }

        if (isSuccess) {
            context.captureEvent(
                eventName,
                properties)
        }else{
            context.captureFailureEvent(
                eventName,
                properties, error!!
            )
        }
    }


    interface Listener {
        fun inProgress(task: Image)
        fun onUploaded(task: Image)
        fun onUploadFail(task: Image)
        fun onConnectionLost()
    }

}