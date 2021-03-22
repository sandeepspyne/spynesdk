package com.spyneai.interfaces

import SubcategoriesResponse
import UploadPhotoResponse
import com.spyneai.aipack.*
import com.spyneai.model.ai.*
import com.spyneai.model.beforeafter.BeforeAfterResponse
import com.spyneai.model.carreplace.AddCarLogoResponse
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.channel.BackgroundsResponse
import com.spyneai.model.channel.ChannelResponse
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.channels.MarketplaceResponse
import com.spyneai.model.dashboard.DashboardResponse
import com.spyneai.model.login.LoginRequest
import com.spyneai.model.login.LoginResponse
import com.spyneai.model.marketupdate.ShootMarketUpdateRequest
import com.spyneai.model.nextsku.SkuRequest
import com.spyneai.model.order.PlaceOrderResponse
import com.spyneai.model.orders.MyOrdersResponse
import com.spyneai.model.ordersummary.OrderSummaryResponse
import com.spyneai.model.otp.OtpRequest
import com.spyneai.model.otp.OtpResponse
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.model.shoot.*
import com.spyneai.model.sku.SkuResponse
import com.spyneai.model.skuedit.EditSkuRequest
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.model.skustatus.UpdateSkuStatusRequest
import com.spyneai.model.skustatus.UpdateSkuStatusResponse
import com.spyneai.model.upload.PreviewResponse
import com.spyneai.model.upload.UploadResponse
import com.spyneai.model.uploadRough.UploadPhotoRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface APiService {
    //-- Cliq APIs --
    @POST("user/registeration")
    fun loginEmailApp(@Body loginRequest: LoginRequest?): Call<LoginResponse>?

    @GET("user/registeration")
    fun loginEmailApp(
        @Query("emailId") emailId: String?,
        @Query("type") type: String?
    ): Call<LoginResponse>?

    @POST("user/phone")
    fun loginApp(@Body loginRequest: LoginRequest?): Call<LoginResponse>?

   /* @POST("user/signin")
    fun postOtp(@Header("tokenId") tokenId: String?, @Body userOtp: OtpRequest): Call<OtpResponse>?
*/
    @GET("credit-user/validate-otp")
    fun postOtp(@Query("emailId") emailId: String?,
                @Query("otp") otp: String?):
           Call<OtpResponse>?

    @GET("credit-user/send-download-mail")
    fun sendEmail(@Query("emailId") emailId: String?,
                @Query("gifLink") otp: String?):
           Call<OtpResponse>?

    @POST("v2/app/send-shoot-results")
    fun sendEmailAll(@Body sendEmailRequest: SendEmailRequest?):
           Call<OtpResponse>?

    @GET("categories/default")
    fun getCategories(@Header("tokenId") tokenId: String?): Call<CategoriesResponse>?

    @GET("categories/{catId}/products")
    fun getSubCategories(
        @Header("tokenId") tokenId: String?,
        @Path("catId") catID: String?
    ): Call<SubcategoriesResponse>?

    @GET("categories/{cat}/products/{product}/markets")
    fun getChannels(
        @Header("tokenId") tokenId: String?,
        @Path("cat") catId: String?,
        @Path("product") proId: String?
    ): Call<ChannelResponse>?

    //GET Shoot id
    @POST("shoot/")
    fun createCollection(
        @Header("tokenId") tokenId: String?,
        @Body createCollectionRequest: CreateCollectionRequest?
    ):
            Call<CreateCollectionResponse>?

    //Update category with shootid
    @PUT("categories/")
    fun updateShootCategory(
        @Header("tokenId") tokenId: String?,
        @Body updateShootCategoryRequest: UpdateShootCategoryRequest
    ):
            Call<CreateCollectionResponse>?

    //Update product with shootid
    @PUT("categories/products")
    fun updateShootProduct(
        @Header("tokenId") tokenId: String?,
        @Body updateShootProductRequest: UpdateShootProductRequest?
    ):Call<CreateCollectionResponse>?


    @POST("shoot/sku-v2")
    fun updateSku(
        @Header("tokenId") tokenId: String?,
        @Body updateSkuRequest: UpdateSkuRequest?
    ): Call<UpdateSkuResponse>?

    /*  @Multipart
      @POST("sku/photo-upload")
      fun uploadPhotoRough(
              @Header("tokenId") tokenId: String?,
              @Part file: MultipartBody.Part?,
              @Part("request") skuName: RequestBody?
      ): Call<UploadPhotoResponse>?*/


    @POST("sku/photo-upload-v3")
    fun uploadPhotoRough(
        @Header("tokenId") tokenId: String?,
        @Body uploadPhotoRequest: UploadPhotoRequest?
    ): Call<UploadPhotoResponse>?

    @GET("sku/continue-shoot")
    fun getContinueShoot(
        @Header("tokenId") tokenId: String?,
        @Query("skuId") skuId: String?)
            : Call<UploadPhotoResponse>?

    @GET("order/skus")
    fun getOrderList(
        @Header("tokenId") tokenId: String?,
        @Query("shootId") shootId: String?,
        @Query("skuId") skuId: String?
    )
            : Call<PlaceOrderResponse>?

    @DELETE("sku")
    fun deleteSku(
        @Header("tokenId") tokenId: String?,
        @Query("shootId") shootId: String?,
        @Query("skuId") skuId: String?
    )
            : Call<PlaceOrderResponse>?

    @PUT("sku/name")
    fun editSku(
        @Header("tokenId") tokenId: String?,
        @Body editSkuRequest: EditSkuRequest?
    ): Call<SkuResponse>?


    @PUT("shoot/sku-status")
    fun updateSkuStauts(
        @Header("tokenId") tokenId: String?,
        @Body updateSkuStatusRequest: UpdateSkuStatusRequest
    ): Call<UpdateSkuStatusResponse>?


    @GET("sku")
    fun getSkuDetails(
        @Header("tokenId") tokenId: String?,
        @Query("skuId") skuId: String?
    ): Call<SkuResponse>?

    @POST("shoot/sku")
    fun getNextShoot(
        @Header("tokenId") tokenId: String?,
        @Body skuRequest: SkuRequest?
    ): Call<UploadPhotoResponse>?

    @GET("categories/{cat}/products/{product}/markets")
    fun getMarketList(
        @Header("tokenId") tokenId: String?,
        @Path("cat") catId: String?,
        @Path("product") proId: String?
    ): Call<MarketplaceResponse>?

    @GET("categories/sub/{categoryId}")
    fun getBeforeAfter(
        @Header("tokenId") tokenId: String?,
        @Path("categoryId") categoryId: String?
    ): Call<BeforeAfterResponse>?

/*
    @PUT("categories/marketplace")
    fun updateMarket(@Header("tokenId") tokenId: String?,
                     @Body shootMarketUpdateRequest: ShootMarketUpdateRequest?
    ): Call<UploadPhotoResponse>?*/

    @PUT("shoot/marketplace")
    fun updateMarket(@Header("tokenId") tokenId: String?,
                     @Body shootMarketUpdateRequest: ShootMarketUpdateRequest?
    ): Call<UploadPhotoResponse>?

    @GET("shoot/v2")
    fun getDashboardData(
        @Header("tokenId") tokenId: String?
    ): Call<DashboardResponse>?

    @GET("order/v2")
    fun getMyOrders(@Header("tokenId") tokenId: String?)
            : Call<MyOrdersResponse>?

    @GET("order/summary")
    fun getOrderSummary(@Header("tokenId") tokenId: String?,
                        @Query("shootId") shootId: String?,
                        @Query("skuId") skuId: String?)
            : Call<OrderSummaryResponse>?




    //-- AI APIs --ok
    @GET("fetch-marketplace?api_key=fde46c58-5735-4fcf-8b38-980c95001dc3")
    fun getChannelsList(@Header("category") category: String?)
            : Call<List<ChannelsResponse>>?

    @GET("fetch-backgrounds?api_key=fde46c58-5735-4fcf-8b38-980c95001dc3")
    fun getBackgroundsList(@Header("category") category: String?)
            : Call<List<BackgroundsResponse>>?


    @GET("car-replacement-plans?api_key=fde46c58-5735-4fcf-8b38-980c95001dc3")
    fun getBackgroundCars(): Call<List<CarBackgroundsResponse>>?

    /* @Multipart
     @POST("upload")
     fun uploadPhoto(
             @Part("image ") image: RequestBody,
             @Part("optimization") optimization: RequestBody?
     ): Call<UploadResponse>?*/

    @Multipart
    @POST("upload?api_key=fde46c58-5735-4fcf-8b38-980c95001dc3")
    fun uploadPhoto(
        @Part file: MultipartBody.Part,
        @Part("optimization") optimization: RequestBody?
    ): Call<UploadResponse>?

    @Multipart
    @POST("shadow-over-footwear?api_key=fde46c58-5735-4fcf-8b38-980c95001dc3")
    fun previewPhoto(
        @Part("image_url") image_url: RequestBody?)
            : Call<PreviewResponse>?

    @Multipart
    @POST("replace-car-bg?api_key=fde46c58-5735-4fcf-8b38-980c95001dc3")
    fun previewPhotoCar(
        @Part("bg_replacement_backgound_id") bg_replacement_backgound_id: RequestBody?,
        @Part("image_url") image_url: RequestBody?,
        @Part("car_bg_replacement_angle") car_bg_replacement_angle: RequestBody?,
    ): Call<PreviewResponse>?

    @Multipart
    @POST("dealership-logo")
    fun previewPhotoCarLogo(
        @Part file: MultipartBody.Part,
        @Part("logo-position") logo_position: RequestBody?,
        @Part("image_url") image_url: RequestBody?,
    ): Call<AddCarLogoResponse>?

    @Multipart
    @POST("bulk-car-reaplacement")
    fun bulkUPload(
            @Part("background") background: RequestBody?,
            @Part("user_id") user_id: RequestBody?,
            @Part("sku_id") sku_id: RequestBody?,
            @Part("image_url") image_url: RequestBody?,
            @Part("sku_name") sku_name: RequestBody?,
            @Part("window_status") window_status: RequestBody?,
            @Part("contrast") contrast: RequestBody?,
    ): Call<BulkUploadResponse>?

    @Multipart
    @POST("fetch-user-bulk-image")
    fun fetchBulkImage(
            @Part("user_id") user_id: RequestBody?,
            @Part("sku_id") sku_id: RequestBody?,
    ): Call<List<FetchBulkResponse>>?

    @Multipart
    @POST("interior-processing")
    fun addWaterMark(
        @Part("background") background: RequestBody?,
        @Part("user_id") user_id: RequestBody?,
        @Part("sku_id") sku_id: RequestBody?,
        @Part("image_url") image_url: RequestBody?,
        @Part("sku_name") sku_name: RequestBody?
    ): Call<WaterMarkResponse>?

    @POST("create_gif/")
    fun fetchGif(
        @Body fetchGifRequest: FetchGifRequest
    ): Call<FetchGifResponse>?

    @GET("fetch-gif")
    fun getGifsList()
            : Call<List<GifResponse>>?

    @Multipart
    @POST("fetch-sku-name")
    fun getCompletedProjects(@Part("user_id") user_id: RequestBody?)
    : Call<List<CompletedProjectResponse>>?

    @Multipart
    @POST("upload-user-gif")
    fun uploadUserGif(
            @Part("user_id") user_id: RequestBody?,
            @Part("sku_id") sku_id: RequestBody?,
            @Part("gif_url") gif_url: RequestBody?)
    : Call<UploadGifResponse>?

    @Multipart
    @POST("fetch-user-gif")
    fun fetchUserGif(
            @Part("user_id") user_id: RequestBody?,
            @Part("sku_id") sku_id: RequestBody?)
    : Call<List<GifFetchResponse>>?
}
