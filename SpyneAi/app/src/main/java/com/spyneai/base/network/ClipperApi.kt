package com.spyneai.base.network

import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.model.carbackgroundgif.CarBackgrounGifResponse
import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetImagesOfSkuResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.shoot.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ClipperApi {

    @Multipart
    @POST("v2/image/upload")
    suspend fun uploadImage(
        @Part("project_id") project_id: RequestBody?,
        @Part("sku_id") sku_id: RequestBody?,
        @Part("image_category") image_category: RequestBody?,
        @Part("auth_key") auth_key: RequestBody?,
        @Part("frame_seq_no") frame_seq_no: Int,
        @Part file: MultipartBody.Part
    ): UploadImageResponse

    @Multipart
    @POST("v2/image/upload")
    fun uploadImageInWorker(
        @Part("project_id") project_id: RequestBody?,
        @Part("sku_id") sku_id: RequestBody?,
        @Part("image_category") image_category: RequestBody?,
        @Part("auth_key") auth_key: RequestBody?,
        @Part file: MultipartBody.Part
    ): Call<UploadImageResponse>

    @Multipart
    @POST("fetch-sku-name")
    suspend fun getCompletedProjects(@Part("user_id") user_id: RequestBody?)
            : List<CompletedProjectResponse>

    @GET("v2/product/fetch")
    suspend fun getCategories(@Query(
        "auth_key") authKey : String): NewCategoriesResponse

    @GET("v2/prod/sub/fetch")
    suspend fun getSubCategories(
        @Query("auth_key") authKey : String,
        @Query("prod_id") prodId : String
    ): NewSubCatResponse

    @GET("v2/overlays/fetch")
    suspend fun getOverlays(@Query("auth_key") authKey : String,
                            @Query("prod_id") prodId : String,
                            @Query("prod_sub_cat_id") prodSubcatId : String,
                            @Query("no_of_frames") frames : String,) : OverlaysResponse

    @FormUrlEncoded
    @POST("v2/project/create")
    suspend fun createProject(@Field("auth_key") authKey : String,
                              @Field("project_name") projectName : String,
                              @Field("prod_cat_id") prodCatId : String) : CreateProjectRes

    @FormUrlEncoded
    @POST("v2/sku/create")
    suspend fun createSku(@Field("auth_key") authKey : String,
                              @Field("project_id") projectId : String,
                              @Field("prod_cat_id") prodCatId : String,
                          @Field("prod_sub_cat_id") prodSubCatId : String,
                          @Field("sku_name") skuName : String,
        @Field("total_frames") totalFrames : Int) : CreateSkuRes

    @GET("v2/sku/updateTotalFrames")
    suspend fun updateTotalFrames(
        @Query("auth_key") authKey : String,
        @Query("sku_id") skuId : String,
        @Query("total_frames") totalFrames : String
    ) : UpdateTotalFramesRes

    @Multipart
    @POST("v2/backgrounds/fetchEnterpriseBgs")
    suspend fun getBackgroundGifCars(
        @Part("category") category: RequestBody?,
        @Part("auth_key") auth_key: RequestBody?,
    ) : CarsBackgroundRes

    @FormUrlEncoded
    @POST("v2/sku/processImages")
    suspend fun processSku(
        @Field("auth_key") authKey : String,
        @Field("sku_id") skuId : String,
        @Field("background_id") backgroundId : String
    ) : ProcessSkuRes


    @FormUrlEncoded
    @POST("v2/sku/processImages")
    fun processSkuWithWorker(
        @Field("auth_key") authKey : String,
        @Field("sku_id") skuId : String,
        @Field("background_id") backgroundId : String
    ) : Call<ProcessSkuRes>


    @GET("v2/sku/getOngoingSKU")
    suspend fun getOngoingSKUs(
        @Query("auth_key") authKey : String
    ) : GetOngoingSkusResponse

    @GET("v2/sku/getCompSKU")
    suspend fun getCompletedSkus(
        @Query("auth_key") authKey: String
    ) : CompletedSKUsResponse

    @POST("v2/sku/getImagesByName")
    suspend fun getImagesOfSku(
        @Field("sku_id") skuId : String,
        @Field("auth_key") authKey : String
    ) : GetImagesOfSkuResponse




}