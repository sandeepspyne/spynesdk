package com.spyneai.credits

import com.spyneai.credits.model.*
import com.spyneai.videorecording.model.VideoProcessingResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface CreditApiService {

    @GET("credit")
    fun getThreeSixtyInteriorByShootId(
    ): Call<CreditPlansRes>?

    @POST("order/credit/")
    fun createOrder(@Body body: CreateOrderBody) : Call<CreateOrderResponse>?

    @PUT("reduce-credit")
    fun reduceCredit(
        @Part("user_id") userId : RequestBody,
        @Part("credit_reduce") creditReduce: RequestBody?,
        @Part("enterprise_id") enterpriseId: RequestBody?,
        @Part("sku_id") skuId: RequestBody?
    ): Call<ReduceCreditResponse>?

    @GET("credit/insert-user")
    fun createCreditPurchaseLog(
        @Query("userId") userId: String,
        @Query("creditId") creditId: String,
        @Query("creditAlloted") creditAlloted: String,
        @Query("creditUsed") creditUsed: Int = 0,
        @Query("creditAvailable") creditAvailable: Int
    ): Call<CreditPurchaseLogRes>?

    @GET("credit/insert-user")
    fun updatePurchasedCredit(
        @Query("userId") userId: String,
        @Query("creditAlloted") creditAlloted : String,
        @Query("creditUsed") creditUsed: Int = 0,
        @Query("creditAvailable") creditAvailable: Int
    ): Call<UpdatePurchaseCreditRes>?
}