package com.spyneai.dashboard.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spyneai.dashboard.network.ClipperApiClient
import com.spyneai.dashboard.network.DashboardApi
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.network.SpyneAiApiClient
import com.spyneai.interfaces.APiService
import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.categories.Data
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.Payload
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response

class DashboardRepository() : BaseRepository() {

    private var spyneApi = SpyneAiApiClient().getClient()

    private var clipperApi = ClipperApiClient().getClient()

    suspend fun getCategories(
        tokenId: String
    ) = safeApiCall {
        clipperApi.getCategories("fde46c58-5735-4fcf-8b38-980c95001dc3")
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