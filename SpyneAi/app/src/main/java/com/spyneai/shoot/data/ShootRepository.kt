package com.spyneai.shoot.data

import com.spyneai.dashboard.data.repository.BaseRepository
import com.spyneai.dashboard.network.ClipperApiClient

class ShootRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()

    suspend fun getSubCategories(
        authKey : String,prodId : String
    ) = safeApiCall {
        clipperApi.getSubCategories(authKey, prodId)
    }
}