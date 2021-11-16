package com.spyneai.credits

import com.spyneai.credits.model.*
import retrofit2.Call
import retrofit2.http.*

interface CreditApiService {

    @GET("credit")
    fun getCredits(
    ): Call<CreditPlansRes>?

    @POST("order/credit/")
    fun createOrder(@Body body: CreateOrderBody)
    : Call<CreateOrderResponse>?

    @FormUrlEncoded
    @PUT("v2/credit/reduce-user-credit")
    fun reduceCredit(
        @Field("auth_key") authKey : String,
        @Field("credit_reduce") creditReduce:String,
        @Field("sku_id") skuId : String,
        @Field("source") source : String = "App_android",
        @Field("image_id") imageId : String = ""
    ): Call<ReduceCreditResponse>?


    @GET("credit/insert-user")
    fun createCreditPurchaseLog(
        @Query("userId") userId: String,
        @Query("creditId") creditId: String,
        @Query("creditAlloted") creditAlloted: Int,
        @Query("creditUsed") creditUsed: String,
        @Query("creditAvailable") creditAvailable: String
    ): Call<CreditPurchaseLogRes>?

    @FormUrlEncoded
    @PUT("v2/credit/update")
    fun updatePurchasedCredit(
        @Field("auth_key") userId: String,
        @Field("credit_alotted") creditAlloted : Int,
        @Field("credit_used") creditUsed: String,
        @Field("credit_available") creditAvailable: Int
    ): Call<UpdatePurchaseCreditRes>?


    @FormUrlEncoded
    @POST("update-download-status")
    fun updateDownloadStatus(@Field("user_id") userId : String,
                             @Field("sku_id") skuId: String,
                             @Field("enterprise_id") enterpriseId: String,
                             @Field("download_hd") downloadHd: Boolean
    ): Call<DownloadHDRes>?

    @GET("insert-review")
    fun insertReview(@Query("userId") userId: String,
                     @Query("description") description: String,
                     @Query("editedUrl") editedUrl: String,
                     @Query("likes") likes: Boolean,
                     @Query("orgUrl") orgUrl: String,
                     @Query("productCategory") productCategory: String
    ): Call<InsertReviewResponse>?


}