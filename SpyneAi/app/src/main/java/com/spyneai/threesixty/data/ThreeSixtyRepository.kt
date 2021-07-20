package com.spyneai.threesixty.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.threesixty.data.model.VideoDetails
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ThreeSixtyRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()

    suspend fun process360(
        authKey : String,
        videoDetails: VideoDetails
    ) = safeApiCall {

        val requestFile =
            File(videoDetails.videoPath).asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val videoFile =
            MultipartBody.Part.createFormData(
                "video",
                File(videoDetails.videoPath)!!.name,
                requestFile
            )

        clipperApi.process360(
           authKey,
            videoDetails.type,
            videoDetails.projectId!!,
            videoDetails.skuName!!,
            videoDetails.skuId!!,
            videoDetails.categoryName,
            videoDetails.subCategory,
            videoDetails.frames,
            videoFile
        )
    }

    suspend fun getBackgroundGifCars(
        category: RequestBody,
        auth_key: RequestBody
    ) = safeApiCall {
        clipperApi.getBackgroundGifCars(category, auth_key)
    }
}