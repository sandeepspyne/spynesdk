package com.spyneai.base.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Url

interface GCPApi {

    @PUT
    suspend fun uploadVideo(
        @Header("content-type") contentType: String,
        @Url uploadUrl: String,
        @Body file: RequestBody
    ): ResponseBody
}