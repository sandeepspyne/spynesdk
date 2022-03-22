package com.spyneai.threesixty.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.GCPApiClient
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.threesixty.data.model.PreSignedVideoBody
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

    suspend fun getVideoPreSignedUrl(
        map: HashMap<String, Any>
    )= safeApiCall {
        clipperApi.getVideoPreSignedUrl(map)
    }

//    suspend fun uploadVideo(
//        contentType : String,
//        url : String,
//        file : MultipartBody.Part
//    ) = safeApiCall {
//        clipperApi.uploadVideo(
//            contentType,
//            url,
//            file
//        )
//    }

    suspend fun setStatusUploaded(
        videoId: String
    ) = safeApiCall {
        clipperApi.setStatusUploaded(videoId)
    }

    suspend fun getBackgroundGifCars(
        category: String
    ) = safeApiCall {
        clipperApi.getBackgroundGifCars(category)
    }

    suspend fun getUserCredits(
        userId : String
    )= safeApiCall {
        clipperApi.userCreditsDetails(userId)
    }

    suspend fun reduceCredit(
        userId : String,
        creditReduce:String,
        skuId: String
    )= safeApiCall {
        clipperApi.reduceCredit(userId, creditReduce,skuId)
    }

    suspend fun updateDownloadStatus(
        userId : String,
       skuId: String,
        enterpriseId: String,
         downloadHd: Boolean
    )= safeApiCall {
        clipperApi.updateDownloadStatus(userId,skuId, enterpriseId, downloadHd)
    }

    suspend fun uploadVideoToGcp(
        fileUrl : String,
        file : RequestBody
    ) = safeApiCall {
        GCPApiClient().getClient().uploadFileToGcp(fileUrl,file)
    }
}