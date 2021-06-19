package com.spyneai.dashboard.data.repository


import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.SpyneAiApiClient

class DashboardRepository() : BaseRepository() {

    private var spyneApi = SpyneAiApiClient().getClient()
    private var clipperApi = ClipperApiClient().getClient()

    suspend fun getCategories(
        auth_key: String
    ) = safeApiCall {
        clipperApi.
        getCategories(auth_key)
    }

    suspend fun getOngoingSKUs(
        tokenId: String
    ) = safeApiCall{
        clipperApi.getOngoingSKUs(tokenId)
    }

    suspend fun getCompletedProjects(
        auth_key : String
    ) = safeApiCall {
        clipperApi.getCompletedSkus(auth_key)
    }

}