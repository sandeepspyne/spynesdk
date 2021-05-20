package com.spyneai.dashboard.network

import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.model.projects.CompletedProjectResponse
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
}