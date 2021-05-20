package com.spyneai.dashboard.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spyneai.dashboard.network.DashboardApi
import com.spyneai.dashboard.network.Resource
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

class DashboardRepository(
    private val api: DashboardApi
) : BaseRepository() {

    suspend fun getCategories(
        tokenId: String
    ) = safeApiCall {
        api.getCategories(tokenId)
    }

    suspend fun createCollection(
        tokenId: String,
        createCollectionRequest: CreateCollectionRequest
    ) = safeApiCall {
        api.createCollection(tokenId, createCollectionRequest)
    }

    suspend fun updateShootCategory(
        tokenId: String,
        updateShootCategoryRequest: UpdateShootCategoryRequest
    ) = safeApiCall {
        api.updateShootCategory(tokenId, updateShootCategoryRequest)
    }

    suspend fun UserFreeCreditEligiblityCheck(
        user_id: RequestBody?,
        email_id: RequestBody?,
    ) = safeApiCall {
        api.UserFreeCreditEligiblityCheck(user_id, email_id)
    }

    suspend fun getCompletedProjects(
        user_id: RequestBody?
    ) = safeApiCall {
        api.getCompletedProjects(user_id)
    }

}