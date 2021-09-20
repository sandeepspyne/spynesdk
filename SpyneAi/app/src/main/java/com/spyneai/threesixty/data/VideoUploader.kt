package com.spyneai.threesixty.data

import android.content.Context
import androidx.core.net.toUri
import com.posthog.android.Properties
import com.spyneai.*
import com.spyneai.base.network.Resource
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.log
import com.spyneai.shoot.utils.logUpload
import com.spyneai.threesixty.data.model.PreSignedVideoBody
import com.spyneai.threesixty.data.model.VideoDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import java.io.File
import okhttp3.MultipartBody.Part.Companion.create
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody


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
                            startNextUpload(video,false,imageType)
                        }
                        captureEvent(Events.UPLOAD_FAILED_SERVICE,video,false,"Image upload limit reached")
                        logUpload("Upload Skipped Retry Limit Reached")
                        return@launch
                    }

                    logUpload("Upload Started "+imageType+" "+video.itemId)

                    val authKey = Utilities.getPreference(context, AppConstants.AUTH_KEY).toString()

                    if (video.isUploaded == 0){
                        //upload video
                        val response = threeSixtyRepository.getVideoPreSignedUrl(
                            PreSignedVideoBody(
                                Utilities.getPreference(context,AppConstants.AUTH_KEY).toString(),
                                video.projectId!!,
                                video.skuId!!,
                                video.categoryName,
                                video.frames,
                                File(video.videoPath).name,
                                video.backgroundId?.toInt()!!
                            )
                        )

                        when(response){
                            is Resource.Success -> {
                                captureEvent(Events.UPLOADED_SERVICE,video,true,null)
                                uploadVideoWithPreSignedUrl(
                                    video.itemId!!,
                                    response.value.data.presignedUrl,
                                    response.value.data.videoId
                                )
                            }

                            is Resource.Failure -> {
                                if(response.errorMessage == null){
                                    captureEvent(Events.GET_PRESIGNED_VIDEO_URL_FAILED,video,false,response.errorCode.toString()+": Http exception from server")
                                }else {
                                    captureEvent(Events.GET_PRESIGNED_VIDEO_URL_FAILED,video,false,response.errorCode.toString()+": "+response.errorMessage)
                                }

                                selectLastImageAndUpload(imageType,retryCount+1)
                            }
                        }

                    }else{
                        //set sku status to uploaded
                    }

                    var response = threeSixtyRepository.process360(authKey,video)

                    when(response){
                        is Resource.Success -> {
                            captureEvent(Events.UPLOADED_SERVICE,video,true,null)
                            startNextUpload(video,true,imageType)
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

    private suspend fun uploadVideoWithPreSignedUrl(itemId : Long, presignedUrl: String, videoId: String) {
        //save presigned url to data base
        localRepository.addPreSignedUrl(
            itemId,
            presignedUrl,
            videoId
        )

        val file: File = File("")

        // create RequestBody instance from file
        val requestFile =
            File("").asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body = create(requestFile)

        //upload video with presigned url
        val uploadVideo = threeSixtyRepository.uploadVideo(
            "application/octet-stream",
            "sandeep singh",
            body
        )


    }

    private fun startNextUpload(itemId: VideoDetails,uploaded : Boolean,imageType : String) {
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