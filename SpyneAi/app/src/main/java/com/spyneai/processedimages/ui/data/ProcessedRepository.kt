package com.spyneai.processedimages.ui.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient

class ProcessedRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()

    suspend fun getImagesOfSku(
        tokenId: String,
        skuId: String
    ) = safeApiCall{
        clipperApi.getImagesOfSku(tokenId, skuId)
    }
}