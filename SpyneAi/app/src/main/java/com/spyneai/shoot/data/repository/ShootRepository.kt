package com.spyneai.shoot.data.repository

import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.SpyneAiApiClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.spyneai.base.BaseRepository

class ShootRepository : BaseRepository() {

    private var spyneApi = SpyneAiApiClient().getClient()
    private var clipperApi = ClipperApiClient().getClient()

    suspend fun uploadImage(
        project_id: RequestBody,
        sku_id: RequestBody,
        image_category: RequestBody,
        auth_key: RequestBody,
        image: MultipartBody.Part
    ) = safeApiCall {
        clipperApi.uploadImage(project_id, sku_id, image_category, auth_key, image)
    }

    suspend fun getSubCategories(
        authKey : String,prodId : String
    ) = safeApiCall {
        clipperApi.getSubCategories(authKey, prodId)
    }

    suspend fun getOverlays(authKey: String, prodId: String,
                            prodSubcategoryId : String, frames : String) = safeApiCall {
        clipperApi.getOverlays(authKey, prodId, prodSubcategoryId, frames)
    }

}