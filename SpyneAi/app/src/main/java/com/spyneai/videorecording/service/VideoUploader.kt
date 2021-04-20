package com.spyneai.videorecording

import android.util.Log
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class VideoUploader(var task : VideoTask, var listener: VideoTaskListener) {

    var TAG = "VideoUploader"

    fun uploadVideo() {

        GlobalScope.launch(Dispatchers.Default){
            try {
                val request = RetrofitClients.buildService(APiService::class.java)

                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), File(task.filePath))
                val video = MultipartBody.Part.createFormData("video", File(task.filePath)!!.name, requestFile)

                val userId = RequestBody.create(
                    MultipartBody.FORM,"sandeep singh"
                )

                val skuName = RequestBody.create(
                    MultipartBody.FORM,"sku name"
                )

                val skuId = RequestBody.create(
                    MultipartBody.FORM,"sku id"
                )

                val type = RequestBody.create(
                    MultipartBody.FORM,"three sixty"
                )

                val category = RequestBody.create(
                    MultipartBody.FORM,"automobiles"
                )

                val call = request.uploadVideo(video,userId,skuName,skuId,type,category)

                call?.enqueue(object : Callback<UploadVideoResponse> {
                    override fun onResponse(
                        call: Call<UploadVideoResponse>,
                        response: Response<UploadVideoResponse>
                    ) {
                        Log.d(TAG, "onResponse: success")
                        if (response.isSuccessful){
                            task.responseUrl = response.body()!!.video_url
                            listener.onSuccess(task)
                        }else{
                            listener.onFailure(task)
                        }
                    }

                    override fun onFailure(call: Call<UploadVideoResponse>, t: Throwable) {

                        Log.d(TAG, "onResponse: failure")
                        listener.onFailure(task)
                    }
                })



            } catch (e: Exception) {

            }
        }
    }

    fun processVideo(){
        GlobalScope.launch(Dispatchers.Default){
            try {
                val request = RetrofitClients.buildService(APiService::class.java)

                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), File(task.filePath))
                val video = MultipartBody.Part.createFormData("video", File(task.filePath)!!.name, requestFile)

                val userId = RequestBody.create(
                    MultipartBody.FORM,"sandeep singh"
                )

                val skuName = RequestBody.create(
                    MultipartBody.FORM,"sku name"
                )

                val skuId = RequestBody.create(
                    MultipartBody.FORM,"sku id"
                )

                val type = RequestBody.create(
                    MultipartBody.FORM,"three sixty"
                )

                val category = RequestBody.create(
                    MultipartBody.FORM,"automobiles"
                )

                val subCategory = RequestBody.create(
                    MultipartBody.FORM,"automobiles"
                )

                val videoUrl= RequestBody.create(
                    MultipartBody.FORM,task.videoUrl
                )

                val call = request.processVideo(video,videoUrl,userId,skuName,skuId,type,category,subCategory)

                call?.enqueue(object : Callback<VideoProcessResponse> {
                    override fun onResponse(
                        call: Call<VideoProcessResponse>,
                        response: Response<VideoProcessResponse>
                    ) {
                        Log.d(TAG, "onResponse: success")
                        if (response.isSuccessful){
                            task.frames = response.body()!!.url
                            listener.onSuccess(task)
                        }else{
                            listener.onFailure(task)
                        }
                    }

                    override fun onFailure(call: Call<VideoProcessResponse>, t: Throwable) {
                        Log.d(TAG, "onResponse: failure")
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