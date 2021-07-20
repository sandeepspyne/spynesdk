package com.spyneai.threesixty.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.threesixty.data.model.VideoDetails
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ThreeSixtyRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()
    private var spyneApi = SpyneAiApiClient().getClient()

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
           authKey.toRequestBody(MultipartBody.FORM),
            videoDetails.type.toRequestBody(MultipartBody.FORM),
            videoDetails.projectId!!.toRequestBody(MultipartBody.FORM),
            videoDetails.skuName!!.toRequestBody(MultipartBody.FORM),
            videoDetails.skuId!!.toRequestBody(MultipartBody.FORM),
            videoDetails.categoryName.toRequestBody(MultipartBody.FORM),
            videoDetails.subCategory.toRequestBody(MultipartBody.FORM),
            videoDetails.frames.toString().toRequestBody(MultipartBody.FORM),
            videoDetails.backgroundId!!.toRequestBody(MultipartBody.FORM),
            videoFile
        )
    }

    suspend fun getBackgroundGifCars(
        category: RequestBody,
        auth_key: RequestBody
    ) = safeApiCall {
        clipperApi.getBackgroundGifCars(category, auth_key)
    }

    suspend fun getUserCredits(
        userId : String
    )= safeApiCall {
        spyneApi.userCreditsDetails(userId)
    }
}