package com.spyneai.base.network

import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.model.shoot.UpdateShootCategoryResponse
import retrofit2.Call
import retrofit2.http.*

interface SpyneApi {

    @GET("credit/user-total-credit")
    suspend fun userCreditsDetails(
        @Query("userId") userId: String
    ): CreditDetailsResponse

}