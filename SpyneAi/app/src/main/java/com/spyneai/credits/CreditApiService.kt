package com.spyneai.credits

import com.spyneai.credits.model.*
import com.spyneai.videorecording.model.VideoProcessingResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
    @PUT("reduce-credit")
    fun reduceCredit(
        @Field("user_id") userId : String,
        @Field("credit_reduce") creditReduce:String,
        @Field("enterprise_id") enterpriseId: String,
        @Field("sku_id") skuId: String
    ): Call<ReduceCreditResponse>?


    @GET("credit/insert-user")
    fun createCreditPurchaseLog(
        @Query("userId") userId: String,
        @Query("creditId") creditId: String,
        @Query("creditAlloted") creditAlloted: Int,
        @Query("creditUsed") creditUsed: String,
        @Query("creditAvailable") creditAvailable: String
    ): Call<CreditPurchaseLogRes>?

    @GET("credit/update-total-credit")
    fun updatePurchasedCredit(
        @Query("userId") userId: String,
        @Query("creditAlloted") creditAlloted : Int,
        @Query("creditUsed") creditUsed: String,
        @Query("creditAvailable") creditAvailable: Int
    ): Call<UpdatePurchaseCreditRes>?

    @GET("download-history")
    fun getHDDownloadStatus(
        @Query("user_id") userId: String,
        @Query("sku_id") skuId : String,
        @Query("enterprise_id") enterpriseId: String = "TaD1VC1Ko"
    ) : Call<DownloadHDRes>?

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