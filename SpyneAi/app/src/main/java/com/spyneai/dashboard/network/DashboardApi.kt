package com.spyneai.dashboard.network

import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.model.shoot.UpdateShootCategoryResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface DashboardApi {

    @GET("categories/default")
    suspend fun getCategories(@Header("tokenId") tokenId: String?
    ): CategoriesResponse

    @POST("shoot/")
    suspend fun createCollection(
        @Header("tokenId") tokenId: String?,
        @Body createCollectionRequest: CreateCollectionRequest?
    ): CreateCollectionResponse

    @PUT("categories/")
    suspend fun updateShootCategory(
        @Header("tokenId") tokenId: String?,
        @Body updateShootCategoryRequest: UpdateShootCategoryRequest
    ): UpdateShootCategoryResponse

    @Multipart
    @POST("insert-user")
    suspend fun UserFreeCreditEligiblityCheck(
        @Part("user_id") user_id: RequestBody?,
        @Part("email_id") email_id: RequestBody?
    ): FreeCreditEligblityResponse

    @Multipart
    @POST("fetch-sku-name")
    suspend fun getCompletedProjects(@Part("user_id") user_id: RequestBody?)
            : List<CompletedProjectResponse>
}