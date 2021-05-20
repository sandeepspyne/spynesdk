package com.spyneai.dashboard.network

import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.model.shoot.UpdateShootCategoryResponse
import retrofit2.http.*

interface SpyneApi {

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

}