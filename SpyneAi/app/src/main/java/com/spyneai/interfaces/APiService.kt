package com.spyneai.interfaces

import com.spyneai.downloadsku.FetchBulkResponseV2
import com.spyneai.model.ai.GifFetchResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.model.credit.UpdateCreditResponse
import com.spyneai.model.ordersummary.OrderSummaryResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface APiService {









    @GET("order/summary")
    fun getOrderSummary(@Header("tokenId") tokenId: String?,
                        @Query("shootId") shootId: String?,
                        @Query("skuId") skuId: String?)
            : Call<OrderSummaryResponse>?


    @Multipart
    @POST("v2/sku/getImagesById")
    fun fetchBulkImageV2(
        @Part("sku_id") sku_id: RequestBody?,
        @Part("auth_key") auth_key: RequestBody?,
    ): Call<FetchBulkResponseV2>?





    @Multipart
    @POST("fetch-user-gif")
    fun fetchUserGif(
        @Part("user_id") user_id: RequestBody?,
        @Part("sku_id") sku_id: RequestBody?
    )
            : Call<List<GifFetchResponse>>?


    @GET("v2/credit/fetch")
    fun userCreditsDetails(
        @Query("auth_key") userId: String?,
    ): Call<CreditDetailsResponse>?

    @Multipart
    @PUT("update-user-credit")
    fun userUpdateCredit(
        @Part("user_id") user_id: RequestBody?,
        @Part("credit_available") credit_available: RequestBody?,
        @Part("credit_used") credit_used: RequestBody?
    ): Call<UpdateCreditResponse>?

}