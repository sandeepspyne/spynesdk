package com.spyneai.credits

import com.spyneai.credits.model.CreateOrderBody
import com.spyneai.credits.model.CreateOrderResponse
import com.spyneai.credits.model.CreditPlansRes
import com.spyneai.videorecording.model.VideoProcessingResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CreditApiService {

    @GET("credit")
    fun getThreeSixtyInteriorByShootId(
    ): Call<CreditPlansRes>?

    @POST("order/credit/")
    fun createOrder(@Body body: CreateOrderBody) : Call<CreateOrderResponse>?
}