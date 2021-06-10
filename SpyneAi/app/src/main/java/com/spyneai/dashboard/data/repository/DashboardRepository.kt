package com.spyneai.dashboard.data.repository


import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import okhttp3.RequestBody

class DashboardRepository() : BaseRepository() {

    private var spyneApi = SpyneAiApiClient().getClient()
    private var clipperApi = ClipperApiClient().getClient()

    suspend fun getCategories(
        tokenId: String
    ) = safeApiCall {
        clipperApi.
        getCategories("fde46c58-5735-4fcf-8b38-980c95001dc3")
    }

    suspend fun createCollection(
        tokenId: String,
        createCollectionRequest: CreateCollectionRequest
    ) = safeApiCall {
        spyneApi.createCollection(tokenId, createCollectionRequest)
    }

    suspend fun updateShootCategory(
        tokenId: String,
        updateShootCategoryRequest: UpdateShootCategoryRequest
    ) = safeApiCall {
        spyneApi.updateShootCategory(tokenId, updateShootCategoryRequest)
    }

    suspend fun UserFreeCreditEligiblityCheck(
        user_id: RequestBody?,
        email_id: RequestBody?,
    ) = safeApiCall {
        clipperApi.UserFreeCreditEligiblityCheck(user_id, email_id)
    }

    suspend fun getCompletedProjects(
        user_id: RequestBody?
    ) = safeApiCall {
        clipperApi.getCompletedProjects(user_id)
    }

}