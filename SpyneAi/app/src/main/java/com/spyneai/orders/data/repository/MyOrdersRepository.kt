package com.spyneai.orders.data.repository

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.orders.data.response.ProjectCountResponse

class MyOrdersRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()

    suspend fun getProjectCount(
        tokenId: String
    ) = ProjectCountResponse(12,32)

    suspend fun getOngoingSKUs(
        tokenId: String
    ) = safeApiCall{
        clipperApi.getOngoingSKUs(tokenId)
    }

    suspend fun getCompletedSKUs(
        tokenId: String
    ) = safeApiCall{
        clipperApi.getCompletedSkus(tokenId)
    }

    suspend fun getProjects(
        tokenId: String,
        status: String
    ) = safeApiCall{
        clipperApi.getProjects(tokenId, status)
    }

    suspend fun getDrafts(
        tokenId: String,
        status: String
    ) = safeApiCall{
        clipperApi.getDrafts(tokenId, status)
    }







}