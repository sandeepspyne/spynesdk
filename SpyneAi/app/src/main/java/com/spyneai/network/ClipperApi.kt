package com.spyneai.dashboard.network

import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.model.projects.CompletedProjectResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ClipperApi {

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

    @GET("v2/product/fetch")
    suspend fun getCategories(@Query("auth_key") authKey : String): NewCategoriesResponse

    @GET("v2/prod/sub/fetch")
    suspend fun getSubCategories(
        @Query("auth_key") authKey : String,
        @Query("prod_id") prodId : String
    ): NewSubCatResponse

    @GET("v2/overlays/fetch")
    suspend fun getOverlays(@Query("auth_key") authKey : String,
                    @Query("prod_id") prodId : String,
                    @Query("prod_sub_cat_id") prodSubcatId : String,
                    @Query("no_of_frames") frames : String,) : OverlaysResponse
}