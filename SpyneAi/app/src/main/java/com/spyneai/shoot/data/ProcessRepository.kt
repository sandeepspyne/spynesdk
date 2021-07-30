package com.spyneai.shoot.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import okhttp3.RequestBody

class ProcessRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()


    suspend fun getBackgroundGifCars(
        category: RequestBody,
        auth_key: RequestBody
    ) = safeApiCall {
        clipperApi.getBackgroundGifCars(category, auth_key)
    }

    suspend fun processSku(authKey : String,skuId : String, backgroundId : String,is360 : Boolean)
            = safeApiCall {
        clipperApi.processSku(authKey, skuId, backgroundId,is360)
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
        creditReduce:String
    )= safeApiCall {
        clipperApi.reduceCredit(userId, creditReduce)
    }

    suspend fun updateDownloadStatus(
        userId : String,
        skuId: String,
        enterpriseId: String,
        downloadHd: Boolean
    )= safeApiCall {
        clipperApi.updateDownloadStatus(userId,skuId, enterpriseId, downloadHd)
    }

}