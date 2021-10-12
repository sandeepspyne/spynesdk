package com.spyneai.shoot.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.ClipperApiStagingClient
import okhttp3.RequestBody

class ProcessRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()
    private var clipperStagingApi = ClipperApiStagingClient().getClient()


    suspend fun getBackgroundGifCars(
        category: RequestBody,
        auth_key: RequestBody
    ) =         safeApiCall {
        clipperApi.getBackgroundGifCars(category, auth_key)
    }



    suspend fun processSku(authKey : String,skuId : String, backgroundId : String,is360 : Boolean,
                           numberPlateBlur: Boolean, windowCorrection: Boolean)
            = safeApiCall {
        clipperApi.processSku(authKey, skuId, backgroundId,is360, numberPlateBlur, windowCorrection)
    }

    suspend fun updateTotalFrames(
        authKey: String,
        skuId : String,
        totalFrames: String
    ) = safeApiCall {
        clipperApi.updateTotalFrames(authKey,skuId,totalFrames)
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

    suspend fun skuProcessStateWithBackgroundId(
        auth_key: String,
        project_id:  String,
        background_id:  Int
    ) = safeApiCall{
        clipperApi.skuProcessStateWithBackgroundId(auth_key, project_id, background_id)
    }

}