package com.spyneai.threesixty.data

import android.content.Context
import com.posthog.android.Properties
import com.spyneai.*
import com.spyneai.base.network.Resource
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.log
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.utils.logUpload
import com.spyneai.threesixty.data.model.VideoDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class VideoUploader(val context: Context,
                    val localRepository : VideoLocalRepository,
                    val threeSixtyRepository: ThreeSixtyRepository,
                    var listener: Listener) {


    fun start() {
        selectLastImageAndUpload(AppConstants.REGULAR,0)
    }

    private fun selectLastImageAndUpload(imageType : String,retryCount : Int) {
        if (context.isInternetActive()){
            GlobalScope.launch(Dispatchers.Default) {

                var skipFlag = -1
                val video = if (imageType == AppConstants.REGULAR){
                    localRepository.getOldestVideo()
                } else{
                    skipFlag = -2
                    localRepository.getOldestSkippedVideo()
                }

                if (video.itemId != null){
                    //uploading enqueued
                    listener.inProgress(video)

                    if (retryCount > 4) {
                        if (video.itemId != null){

                            localRepository.skipVideo(video.itemId!!,skipFlag)
                            startNextUpload(video.itemId!!,false,imageType)
                        }
                        captureEvent(Events.UPLOAD_FAILED_SERVICE,video,false,"Image upload limit reached")
                        logUpload("Upload Skipped Retry Limit Reached")
                        return@launch
                    }

//                   if (image.imagePath != null){
//                       if (!File(image.imagePath!!).exists()){
//                           localRepository.deleteImage(image.itemId!!)
//                           captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,"Image file got deleted by user")
//                            startNextUpload(image.itemId!!,true,imageType)
//                       }
//                   }

                    logUpload("Upload Started "+imageType+" "+video.itemId)


                    val authKey = Utilities.getPreference(context, AppConstants.AUTH_KEY).toString()

                    var response = threeSixtyRepository.process360(authKey,video)

                    when(response){
                        is Resource.Success -> {
                            captureEvent(Events.UPLOADED_SERVICE,video,true,null)
                            startNextUpload(video.itemId!!,true,imageType)
                        }

                        is Resource.Failure -> {
                            log("Image upload failed")
                            logUpload("Upload error "+response.errorCode.toString()+" "+response.errorMessage)
                            if(response.errorMessage == null){
                                captureEvent(Events.UPLOAD_FAILED_SERVICE,video,false,response.errorCode.toString()+": Http exception from server")
                            }else {
                                captureEvent(Events.UPLOAD_FAILED_SERVICE,video,false,response.errorCode.toString()+": "+response.errorMessage)
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
                        val count = localRepository.updateSkippedVideos()

                        //check if we don"t have any new image clicked while uploading skipped images
                        if (localRepository.getOldestVideo().itemId == null){
                            if (count > 0){
                                //upload double skipped images if we don't have any new image
                                selectLastImageAndUpload(AppConstants.SKIPPED,0)
                            }
                            else
                                listener.onUploaded(video)
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
            localRepository.markUploaded(itemId)

        selectLastImageAndUpload(imageType,0)
    }

    private fun captureEvent(eventName : String, video : VideoDetails, isSuccess : Boolean, error: String?) {
        val properties = Properties()
        properties.apply {
            this["sku_id"] = video.skuId
            this["project_id"] = video.projectId
            this["image_type"] = video.categoryName
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
        fun inProgress(task: VideoDetails)
        fun onUploaded(task: VideoDetails)
        fun onUploadFail(task: VideoDetails)
        fun onConnectionLost()
    }

}