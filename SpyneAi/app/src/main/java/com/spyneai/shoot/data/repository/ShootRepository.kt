package com.spyneai.shoot.data.repository

import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.base.repository.BaseRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

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

}