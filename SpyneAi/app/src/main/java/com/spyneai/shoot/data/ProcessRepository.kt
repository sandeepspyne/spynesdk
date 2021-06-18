package com.spyneai.shoot.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient

class ProcessRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()


    suspend fun getBackgroundGifCars()
            = safeApiCall {
        clipperApi.getBackgroundGifCars()
    }

    suspend fun processSku(authKey : String,skuId : String, backgroundId : String)
            = safeApiCall {
        clipperApi.processSku(authKey, skuId, backgroundId)
    }

}