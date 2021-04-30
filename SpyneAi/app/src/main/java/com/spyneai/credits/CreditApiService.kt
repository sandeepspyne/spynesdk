package com.spyneai.credits

import com.spyneai.credits.model.CreateOrderBody
import com.spyneai.credits.model.CreateOrderResponse
import com.spyneai.credits.model.CreditPlansRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CreditApiService {

    @GET("credit")
    fun getThreeSixtyInteriorByShootId(
    ): Call<CreditPlansRes>?

    @POST("order/credit/")
    fun createOrder(@Body body: CreateOrderBody) : Call<CreateOrderResponse>?
}