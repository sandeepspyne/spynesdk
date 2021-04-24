package com.spyneai.videorecording.service

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.videorecording.model.UploadVideoResponse
import com.spyneai.videorecording.model.VideoProcessResponse
import com.spyneai.videorecording.model.VideoProcessingResponse
import com.spyneai.videorecording.model.VideoTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class VideoUploader(var task : VideoTask, var listener: VideoTaskListener) {

    var TAG = "VideoUploader"

    fun uploadVideo() {
        Log.d(TAG, "uploadVideo: ")

        GlobalScope.launch(Dispatchers.Default){
            try {
                val request = RetrofitClients.buildService(APiService::class.java)

                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), File(task.filePath))
                val video = MultipartBody.Part.createFormData("video", File(task.filePath)!!.name, requestFile)

                val userId = RequestBody.create(
                    MultipartBody.FORM,task.user_id
                )

                val skuName = RequestBody.create(
                    MultipartBody.FORM,"sku name"
                )

                val skuId = RequestBody.create(
                    MultipartBody.FORM,task.skuId
                )

                val type = RequestBody.create(
                    MultipartBody.FORM,"360"
                )

                val category = RequestBody.create(
                    MultipartBody.FORM,"Automobiles"
                )

                val call = request.
                uploadVideo(video,userId,skuName,skuId,type,category)

                call?.enqueue(object : Callback<UploadVideoResponse> {
                    override fun onResponse(
                        call: Call<UploadVideoResponse>,
                        response: Response<UploadVideoResponse>
                    ) {
                        Log.d(TAG, "onResponse: upload success")
                        if (response.isSuccessful){
                            task.responseUrl = response.body()!!.video_url
                            listener.onSuccess(task)
                        }else{
                           Log.d(TAG, "onResponse: upload failure")
                            listener.onFailure(task)
                        }
                    }

                    override fun onFailure(call: Call<UploadVideoResponse>, t: Throwable) {

                        Log.d(TAG, "onResponse: upload failure")
                        listener.onFailure(task)
                    }
                })



            } catch (e: Exception) {

            }
        }
    }

    fun processVideo(){
        Log.d(TAG, "processVideo: ")
        GlobalScope.launch(Dispatchers.Default){
            try {
                val request = RetrofitClients.buildService(APiService::class.java)

                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), File(task.filePath))
                val video = MultipartBody.Part.createFormData("video", File(task.filePath)!!.name, requestFile)

                val userId = RequestBody.create(
                    MultipartBody.FORM,task.user_id
                )

                val skuName = RequestBody.create(
                    MultipartBody.FORM,"sku name"
                )

                val skuId = RequestBody.create(
                    MultipartBody.FORM,task.skuId
                )

                val type = RequestBody.create(
                    MultipartBody.FORM,"360"
                )

                val category = RequestBody.create(
                    MultipartBody.FORM,"Automobiles"
                )

                val subCategory = RequestBody.create(
                    MultipartBody.FORM,"sedan"
                )

                val frames= RequestBody.create(
                    MultipartBody.FORM,"72"
                )

                val videoUrl= RequestBody.create(
                    MultipartBody.FORM,task.videoUrl
                )

                val call = request.processVideo(video,videoUrl,userId,skuName,skuId,type,category,subCategory,frames)

                call?.enqueue(object : Callback<VideoProcessingResponse> {
                    override fun onResponse(
                        call: Call<VideoProcessingResponse>,
                        response: Response<VideoProcessingResponse>
                    ) {
                        Log.d(TAG, "onResponse:  processVideo success ")
                        if (response.isSuccessful){

                            var videoProcessResponse = response.body()

                            if (videoProcessResponse != null){
                             //   task.frames = response.body()
                                FramesHelper.framesMap.put(task.skuId,videoProcessResponse)
                                listener.onSuccess(task)

                            }else{
                                Log.d(TAG, "onResponse:  processVideo sussess null ")
                                listener.onFailure(task)
                            }

                        }else{
                            Log.d(TAG, "onResponse:  processVideo success fail ")
                            listener.onFailure(task)
                        }
                    }

                    override fun onFailure(call: Call<VideoProcessingResponse>, t: Throwable) {
                        Log.d(TAG, "onResponse: processVideo failure"+t.localizedMessage)
                        listener.onFailure(task)
                    }
                })

            } catch (e: Exception) {

            }
        }
    }

    interface VideoTaskListener {
        fun onSuccess(task: VideoTask)
        fun onFailure(task: VideoTask)
    }
}