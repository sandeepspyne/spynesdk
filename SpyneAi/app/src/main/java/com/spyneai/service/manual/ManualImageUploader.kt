package com.spyneai.service.manual

import android.content.Context
import com.posthog.android.Properties
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.ImageFile
import com.spyneai.shoot.utils.logManualUpload
import com.spyneai.shoot.utils.logUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ManualImageUploader(val context: Context,
                          val filesRepository: FilesRepository,
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
                    filesRepository.getOldestImage()
                } else{
                    skipFlag = -2
                    filesRepository.getOldestSkippedImage()
                }

                if (image.itemId != null){
                    //uploading enqueued
                    listener.inProgress(image)

                    if (retryCount > 4) {
                        if (image.itemId != null){

                            filesRepository.skipImage(image.itemId!!,skipFlag)
                            startNextUpload(image.itemId!!,false,imageType)
                        }
                        captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,"Image upload limit reached")
                        logUpload("Upload Skipped Retry Limit Reached")
                        return@launch
                    }

//                    if (image.imagePath != null){
//                        if (!File(image.imagePath!!).exists()){
//                            filesRepository.deleteImage(image.itemId!!)
//                            captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,"Image file got deleted by user")
//                            startNextUpload(image.itemId!!,true,imageType)
//                        }
//                    }

                    logUpload("Upload Started "+imageType+" "+image.itemId)

                    val authKey =
                        Utilities.getPreference(context, AppConstants.AUTH_KEY).toString()

                    //check upload status
                    var uploadStatuRes = shootRepository.checkUploadStatus(
                        authKey,
                        File(image.imagePath).name,
                    )

                    when(uploadStatuRes) {
                        is Resource.Success -> {

                            logManualUpload("Check Status success "+uploadStatuRes.value.data.upload)
                            image.projectId = uploadStatuRes.value.data.projectId
                            captureEvent(Events.CHECK_UPLOAD_STATUS,image,true,null)

                            if (uploadStatuRes.value.data.upload){
                                //start next image
                                captureEvent(Events.ALREADY_UPLOAD_STATUS,image,true,null)
                                startNextUpload(image.itemId!!,true,imageType)
                            }else {
                                captureEvent(Events.ALREADY_NOT_UPLOAD_STATUS,image,true,null)
                                //make upload call
                                var imageFile: MultipartBody.Part? = null
                                val requestFile =
                                    File(image.imagePath).asRequestBody("multipart/form-data".toMediaTypeOrNull())

                                val fileName = if (image.categoryName == "360int") {
                                    image.skuName + "_" + image.skuId + "_360int_1"
                                }else {
                                    File(image.imagePath)!!.name
                                }

                                imageFile =
                                    MultipartBody.Part.createFormData(
                                        "image",
                                        fileName,
                                        requestFile
                                    )

//                                var response = shootRepository.uploadImage(
//                                    uploadStatuRes.value.data.projectId.toRequestBody(MultipartBody.FORM),
//                                    uploadStatuRes.value.data.skuId.toRequestBody(MultipartBody.FORM),
//                                    uploadStatuRes.value.data.imageCategory.toRequestBody(MultipartBody.FORM),
//                                    authKey.toRequestBody(MultipartBody.FORM),
//                                    "Retry".toRequestBody(MultipartBody.FORM),
//                                    uploadStatuRes.value.data.sequence,
//                                    "".toRequestBody(MultipartBody.FORM),
//                                    imageFile)
//
//                                when(response){
//                                    is Resource.Success -> {
//                                        logManualUpload("Manual upload success")
//                                        captureEvent(Events.MANUALLY_UPLOADED,image,true,null)
//                                        startNextUpload(image.itemId!!,true,imageType)
//                                    }
//
//                                    is Resource.Failure -> {
//                                        logManualUpload("Manual upload failed")
//                                        if(response.errorMessage == null){
//                                            captureEvent(Events.MANUAL_UPLOAD_FAILED,image,false,response.errorCode.toString()+": Http exception from server")
//                                        }else {
//                                            captureEvent(Events.MANUAL_UPLOAD_FAILED,image,false,response.errorCode.toString()+": "+response.errorMessage)
//                                        }
//                                    }
//                                }
                            }
                        }

                        is Resource.Failure -> {
                            logManualUpload("Check Status failed "+ uploadStatuRes.errorMessage)
                            if(uploadStatuRes.errorMessage == null){
                                captureEvent(Events.CHECK_UPLOAD_STATUS_FAILED,image,false,uploadStatuRes.errorCode.toString()+": Http exception from server")
                            }else {
                                captureEvent(Events.CHECK_UPLOAD_STATUS_FAILED,image,false,uploadStatuRes.errorCode.toString()+": "+uploadStatuRes.errorMessage)
                            }
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
                        val count = filesRepository.updateSkipedImages()

                        //check if we don"t have any new image clicked while uploading skipped images
                        if (filesRepository.getOldestImage().itemId == null){
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
            filesRepository.deleteImage(itemId)

        selectLastImageAndUpload(imageType,0)
    }

    private fun captureEvent(eventName : String, image : ImageFile, isSuccess : Boolean, error: String?) {
        val properties = HashMap<String,Any?>()
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
        fun inProgress(task: ImageFile)
        fun onUploaded(task: ImageFile)
        fun onUploadFail(task: ImageFile)
        fun onConnectionLost()
    }

}