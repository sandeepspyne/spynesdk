package com.spyneai.threesixty.data

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.posthog.android.Properties
import com.spyneai.*
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.GcpClient
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.log
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

import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.InputStream


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
                        return@launch
                    }

                    val authKey = Utilities.getPreference(context, AppConstants.AUTH_KEY).toString()

                    val s = ""

                    if (video.isUploaded == 0){
                        //upload video
                        val response = threeSixtyRepository.getVideoPreSignedUrl(
                            PreSignedVideoBody(
                                Utilities.getPreference(context,AppConstants.AUTH_KEY).toString(),
                                video.projectId!!,
                                video.skuId!!,
                                video.categoryName,
                                AppConstants.CARS_CATEGORY_ID,
                                video.frames,
                                File(video.videoPath).name,
                                video.backgroundId?.toInt()!!
                            )
                        )

                        when(response){
                            is Resource.Success -> {
                                captureEvent(Events.UPLOADED_SERVICE,video,true,null)
                                uploadVideoWithPreSignedUrl(
                                    video,
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

//                    var response = threeSixtyRepository.process360(authKey,video)
//
//                    when(response){
//                        is Resource.Success -> {
//                            captureEvent(Events.UPLOADED_SERVICE,video,true,null)
//                            startNextUpload(video,true,imageType)
//                        }
//
//                        is Resource.Failure -> {
//                            if(response.errorMessage == null){
//                                captureEvent(Events.UPLOAD_FAILED_SERVICE,video,false,response.errorCode.toString()+": Http exception from server")
//                            }else {
//                                captureEvent(Events.UPLOAD_FAILED_SERVICE,video,false,response.errorCode.toString()+": "+response.errorMessage)
//                            }
//
//                            selectLastImageAndUpload(imageType,retryCount+1)
//                        }
//                    }
                }else{
                    if (imageType == AppConstants.REGULAR){
                        //start skipped images worker
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

    private fun uploadVideoWithPreSignedUrl(video: VideoDetails, presignedUrl: String, videoId: String) {
        //save presigned url to data base
        localRepository.addPreSignedUrl(
            video.itemId!!,
            presignedUrl,
            videoId
        )


//        val MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8")
//
//        val inputStream: InputStream = File(video.videoPath).inputStream()
//
//        val requestBody: RequestBody = RequestBodyUtil.create(MEDIA_TYPE_MARKDOWN, inputStream)


        // create RequestBody instance from file
        val requestFile =
            File(video.videoPath).asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull())

       // val body = create(requestFile)

        //upload video with presigned url
        val request = GcpClient.buildService(ClipperApi::class.java)

        val call = request.uploadVideo(
            "application/octet-stream",
            presignedUrl,
            requestFile
            )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("VideoUploader", "onResponse: "+response.code())
                localRepository.markUploaded(video)
                localRepository.markStatusUploaded(video)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("VideoUploader", "onFailure: "+t.message)
            }

        })
    }

    private fun startNextUpload(itemId: VideoDetails,uploaded : Boolean,imageType : String) {
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