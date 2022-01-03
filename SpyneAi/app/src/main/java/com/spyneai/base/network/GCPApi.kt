package com.spyneai.base.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Url

interface GCPApi {

    @PUT
    suspend fun uploadFileToGcp(
        @Url uploadUrl: String,
        @Body file: RequestBody,
        @Header("content-type") contentType: String = "application/octet-stream"
    ): ResponseBody

//    abstract fun uploadImageToGcp(path: String, preSignedUrl: String, fileUrl: String)

}