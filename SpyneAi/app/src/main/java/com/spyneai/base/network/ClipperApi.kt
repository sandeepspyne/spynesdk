package com.spyneai.base.network

import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.shoot.data.model.UploadImageResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @Multipart
    @POST("v2/image/upload")
    suspend fun uploadImage(
        @Part("project_id") project_id: RequestBody?,
        @Part("sku_id") sku_id: RequestBody?,
        @Part("image_category") image_category: RequestBody?,
        @Part("auth_key") auth_key: RequestBody?,
        @Part file: MultipartBody.Part
    ): UploadImageResponse

    @GET("v2/prod/sub/fetch")
    suspend fun getSubCategories(
        @Query("auth_key") authKey : String,
        @Query("prod_id") prodId : String
    ): NewSubCatResponse


}