package com.spyneai.dashboard.repository


import androidx.room.Room
import com.spyneai.BaseApplication
import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.ProjectApiClient
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.base.room.AppDatabase
import com.spyneai.dashboard.repository.model.category.DynamicLayout
import com.spyneai.dashboard.response.NewCategoriesResponse
import org.json.JSONObject

class DashboardRepository() : BaseRepository() {

    private var spyneApi = SpyneAiApiClient().getClient()
    private var clipperApi = ClipperApiClient().getClient()


    fun getCategories(): List<NewCategoriesResponse.Category> {
        return Room.databaseBuilder(
            BaseApplication.getContext(),
            AppDatabase::class.java, "spyne-db"
        ).build().dashboardDao().getAll()
    }


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

    //on going completed
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

    fun insertCategories(data: List<NewCategoriesResponse.Category>,dynamicLayout: List<DynamicLayout>): List<Long> {
        return Room.databaseBuilder(
            BaseApplication.getContext(),
            AppDatabase::class.java, "spyne-db"
        ).build().dashboardDao().insert(data)
    }

}