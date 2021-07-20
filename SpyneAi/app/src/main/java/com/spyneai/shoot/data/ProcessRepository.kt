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

    suspend fun processSku(authKey : String,skuId : String, backgroundId : String)
            = safeApiCall {
        clipperApi.processSku(authKey, skuId, backgroundId)
    }

    suspend fun updateTotalFrames(
        authKey: String,
        skuId : String,
        totalFrames: String
    ) = safeApiCall {
        clipperApi.updateTotalFrames(authKey,skuId,totalFrames)
    }

}