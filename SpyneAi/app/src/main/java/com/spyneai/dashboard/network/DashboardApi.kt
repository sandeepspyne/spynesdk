package com.spyneai.dashboard.network

import com.spyneai.model.categories.CategoriesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface DashboardApi {
    @GET("categories/default")
    fun getCategories(@Header("tokenId") tokenId: String?): CategoriesResponse
}