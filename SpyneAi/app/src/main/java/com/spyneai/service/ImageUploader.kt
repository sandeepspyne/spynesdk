package com.spyneai.service

import android.content.Context
import androidx.work.*
import com.posthog.android.Properties
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.utils.logUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ImageUploader(val context: Context,
                    val localRepository : ShootLocalRepository,
                    val shootRepository: ShootRepository,
                    var listener: Listener) {


    fun start() {
        selectLastImageAndUpload(AppConstants.REGULAR,0)
    }

    private fun selectLastImageAndUpload(imageType : String,retryCount : Int) {

       if (context.isInternetActive()){
           GlobalScope.launch(Dispatchers.Default) {

               var skipFlag = -1
               val image = if (imageType == AppConstants.REGULAR){
                   localRepository.getOldestImage()
               } else{
                   skipFlag = -2
                   localRepository.getOldestSkippedImage()
               }

               if (image.itemId != null){
                   //uploading enqueued
                   listener.inProgress(image)

                   if (retryCount > 4) {
                       if (image.itemId != null){

                           localRepository.skipImage(image.itemId!!,skipFlag)
                           startNextUpload(image.itemId!!,false,imageType)
                       }
                       captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,"Image upload limit reached")
                       logUpload("Upload Skipped Retry Limit Reached")
                       return@launch
                   }

                   if (image.imagePath != null){
                       if (!File(image.imagePath!!).exists()){
                           localRepository.deleteImage(image.itemId!!)
                           captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,"Image file got deleted by user")
                            startNextUpload(image.itemId!!,true,imageType)
                       }
                   }

                   logUpload("Upload Started "+imageType+" "+image.itemId)

                   val projectId = image.projectId?.toRequestBody(MultipartBody.FORM)

                   val skuId = image.skuId?.toRequestBody(MultipartBody.FORM)
                   val imageCategory =
                       image.categoryName?.toRequestBody(MultipartBody.FORM)

                   val authKey = Utilities.getPreference(context, AppConstants.AUTH_KEY).toString().toRequestBody(MultipartBody.FORM)

                   var imageFile: MultipartBody.Part? = null
                   val requestFile =
                       File(image.imagePath).asRequestBody("multipart/form-data".toMediaTypeOrNull())

                   val fileName = if (image.categoryName == "360int") {
                       image.skuName + "_" + image.skuId + "_360int_"+image.sequence+".jpg"
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

                    val uploadType = if (retryCount == 0) "Direct" else "Retry"

                   var response = if (image.categoryName == "360int"){
                       shootRepository.uploadImage(projectId!!,
                           skuId!!, imageCategory!!,authKey, uploadType.toRequestBody(MultipartBody.FORM),image.sequence!!,imageFile)
                   }else {
                       shootRepository.uploadImage(projectId!!,
                           skuId!!, imageCategory!!,authKey, uploadType.toRequestBody(MultipartBody.FORM),image.sequence!!,imageFile)
                   }

                   when(response){
                       is Resource.Success -> {
                           captureEvent(Events.UPLOADED_SERVICE,image,true,null)
                           startNextUpload(image.itemId!!,true,imageType)
                       }

                       is Resource.Failure -> {
                          logUpload("Upload error "+response.errorCode.toString()+" "+response.errorMessage)
                           if(response.errorMessage == null){
                               captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,response.errorCode.toString()+": Http exception from server")
                           }else {
                               captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,response.errorCode.toString()+": "+response.errorMessage)
                           }

                           selectLastImageAndUpload(imageType,retryCount+1)
                       }
                   }

               }else{
                   logUpload("All Images uploaded")
                   if (imageType == AppConstants.REGULAR){
                       //start skipped images worker
                       logUpload("Start Skipped Images uploaded")
                      selectLastImageAndUpload(AppConstants.SKIPPED,0)
                   }else{
                       //make second time skipped images elligible for upload
                       val count = localRepository.updateSkipedImages()

                       //check if we don"t have any new image clicked while uploading skipped images
                      if (localRepository.getOldestImage().itemId == null){
                          if (count > 0){
                              //upload double skipped images if we don't have any new image
                              selectLastImageAndUpload(AppConstants.SKIPPED,0)
                          }
                          else
                              listener.onUploaded(image)
                      } else{
                          //upload images clicked while service uploading skipped images
                          selectLastImageAndUpload(AppConstants.REGULAR,0)
                      }

                   }

               }
           }
       }else {
           listener.onConnectionLost()
       }
    }

    private fun startNextUpload(itemId: Long,uploaded : Boolean,imageType : String) {
       logUpload("Start next upload "+uploaded)
        //remove uploaded item from database
        if (uploaded)
            localRepository.deleteImage(itemId)

        selectLastImageAndUpload(imageType,0)
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