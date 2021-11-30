package com.spyneai.dashboard.data.repository


import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.SpyneAiApiClient
import org.json.JSONObject

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

    //ongoign completed
    suspend fun getProjects(
        tokenId: String,
        status: String
    ) = safeApiCall{
        clipperApi.getProjects(tokenId, status)
    }

    suspend fun getVersionStatus(
        authKey: String,
        version : String
    )= safeApiCall {
        clipperApi.getVersionStatus(authKey,version)
    }

    suspend fun getGCPUrl(
        imageName: String
    )= safeApiCall {
        clipperApi.getGCPUrl(imageName)
    }

    suspend fun captureCheckInOut(
        type : String,
        location : JSONObject,
        location_id : String,
        imageUrl : String = ""
    ) = safeApiCall {
        clipperApi.captureCheckInOut(type,location,location_id,imageUrl)
    }

    suspend fun getLocations(
    )= safeApiCall {
        clipperApi.getLocations()
    }

}