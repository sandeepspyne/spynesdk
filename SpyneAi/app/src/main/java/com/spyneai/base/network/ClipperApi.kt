package com.spyneai.base.network

import com.spyneai.BaseApplication
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.credits.model.DownloadHDRes
import com.spyneai.credits.model.ReduceCreditResponse
import com.spyneai.dashboard.data.model.CheckInOutRes
import com.spyneai.dashboard.data.model.GetGCPUrlRes
import com.spyneai.dashboard.data.model.LocationsRes
import com.spyneai.dashboard.data.model.VersionStatusRes
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.reshoot.data.ReshootOverlaysRes
import com.spyneai.service.manual.FilesDataRes
import com.spyneai.shoot.data.model.*
import com.spyneai.shoot.response.SkuProcessStateResponse
import com.spyneai.shoot.response.UpdateVideoSkuRes
import com.spyneai.shoot.response.UploadFolderRes
import com.spyneai.shoot.response.UploadStatusRes
import com.spyneai.threesixty.data.model.VideoPreSignedRes
import com.spyneai.threesixty.data.response.ProcessThreeSixtyRes
import com.spyneai.threesixty.data.response.VideoUploadedRes
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
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
        @Part("upload_type") upload_type: RequestBody?,
        @Part("frame_seq_no") frame_seq_no: Int,
        @Part("tags") tags: RequestBody,
        @Part file: MultipartBody.Part
    ): UploadImageResponse

    @FormUrlEncoded
    @POST("v4/image/upload")
    suspend fun getPreSignedUrl(
        @Field("project_id") project_id: String?,
        @Field("sku_id") sku_id: String?,
        @Field("image_category") image_category: String?,
        @Field("image_name") image_name: String?,
        @Field("overlay_id") overlay_id: Int?,
        @Field("upload_type") upload_type: String?,
        @Field("frame_seq_no") frame_seq_no: Int?,
        @Field("is_reclick") is_reclick: Boolean?,
        @Field("is_reshoot") isReshoot: Boolean?,
        @Field("tags") tags: String?,
        @Field("debug_data") debugData: String?,
        @Field("angle") angle: Int,
        @Field("source") source : String = "App_android",
        @Field("auth_key") authKey : String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString()
    ): ImagePreSignedRes

    @FormUrlEncoded
    @POST("v4/image/mark-done")
    suspend fun markUploaded(
        @Field("image_id") imageId: String,
        @Field("auth_key") authKey : String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString()
    ) : ImagePreSignedRes

    @Multipart
    @POST("v2/image/upload")
    suspend fun uploadDebugImage(
        @Part("project_id") project_id: RequestBody?,
        @Part("sku_id") sku_id: RequestBody?,
        @Part("image_category") image_category: RequestBody?,
        @Part("auth_key") auth_key: RequestBody?,
        @Part("upload_type") upload_type: RequestBody?,
        @Part("frame_seq_no") frame_seq_no: Int,
        @Part file: MultipartBody.Part
    ): UploadImageResponse

    @Multipart
    @POST("v2/image/upload")
    suspend fun uploadImageWithAngle(
        @Part("project_id") project_id: RequestBody?,
        @Part("sku_id") sku_id: RequestBody?,
        @Part("image_category") image_category: RequestBody?,
        @Part("auth_key") auth_key: RequestBody?,
        @Part("upload_type") upload_type: RequestBody?,
        @Part("frame_seq_no") frame_seq_no: Int,
        @Part("angle") angle: Int,
        @Part file: MultipartBody.Part
    ): UploadImageResponse

    @Multipart
    @POST("v2/image/upload")
    fun uploadImageInWorker(
        @Part("project_id") project_id: RequestBody?,
        @Part("sku_id") sku_id: RequestBody?,
        @Part("image_category") image_category: RequestBody?,
        @Part("auth_key") auth_key: RequestBody?,
        @Part("frame_seq_no") frame_seq_no: RequestBody?,
        @Part file: MultipartBody.Part
    ): Call<UploadImageResponse>

    @Multipart
    @POST("fetch-sku-name")
    suspend fun getCompletedProjects(@Part("user_id") user_id: RequestBody?)
            : List<CompletedProjectResponse>

    @GET("v2/product/fetch")
    suspend fun getCategories(@Query(
        "auth_key") authKey : String): NewCategoriesResponse

    @GET("v2/prod/sub/fetch/v2")
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
    @POST("v2/project/create/v2")
    suspend fun createProject(@Field("auth_key") authKey : String,
                              @Field("project_name") projectName : String,
                              @Field("prod_cat_id") prodCatId : String,
                              @Field("dynamic_layout") dynamic_layout: JSONObject? = null,
                              @Field("location_data") location_data: JSONObject? = null,
                              @Field("source") source : String = "App_android") : CreateProjectRes

    @FormUrlEncoded
    @POST("v2/sku/create/v2")
    suspend fun createSku(@Field("auth_key") authKey : String,
                          @Field("project_id") projectId : String,
                          @Field("prod_cat_id") prodCatId : String,
                          @Field("prod_sub_cat_id") prodSubCatId : String,
                          @Field("sku_name") skuName : String,
                          @Field("total_frames") totalFrames : Int,
                          @Field("images") images : Int,
                          @Field("videos") videos : Int,
                          @Field("source") source : String = "App_android"
    ) : CreateSkuRes

    @FormUrlEncoded
    @POST("v3/video/videoimages-update")
    suspend fun updateVideoSku(
        @Field("sku_id") skuId : String,
        @Field("prod_sub_cat_id") prodSubCatId : String,
        @Field("initial_image_count") initialImageCount : Int,
        @Field("auth_key") authKey : String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString(),
        @Field("images") images : Int = 1
    ) : UpdateVideoSkuRes

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
        @Field("background_id") backgroundId : String,
        @Field("is_360") is360 : Boolean,
        @Field("number_plate_blur") numberPlateBlur : Boolean,
        @Field("window_correction") windowCorrection : Boolean,
        @Field("tint_window") tintWindow : Boolean
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

    @FormUrlEncoded
    @POST("v2/sku/getImagesById")
    suspend fun getImagesOfSku(
        @Field("auth_key") authKey : String,
        @Field("sku_id") skuId : String
    ) : ImagesOfSkuRes

    @GET("v2/overlays/fetch-ids")
    suspend fun getOverlayIds(
        @Query("overlay_ids") overlayId : JSONArray,
        @Query("auth_key") authKey : String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString()
    ) : ReshootOverlaysRes

    @GET("v2/project/getSkuPerProject")
    suspend fun getProjectDetail(
        @Query("auth_key") authKey : String,
        @Query("project_id") projectId : String
    ) : ProjectDetailResponse

    @GET("v2/sku/updateTotalFrames")
    suspend fun updateTotalFrames(
        @Query("auth_key") authKey : String,
        @Query("sku_id") skuId : String,
        @Query("total_frames") totalFrames : String
    ) : UpdateTotalFramesRes

    @GET("v3/project/getDetailsProject")
    suspend fun getProjects(
        @Query("auth_key") authKey: String,
        @Query("status") status: String
    ) : GetProjectsResponse

    @GET("v3/project/getDetailsProject")
    suspend fun getDrafts(
        @Query("auth_key") authKey: String,
        @Query("status") status: String
    ) : GetProjectsResponse


    @Multipart
    @POST("v2/video/upload_two")
    suspend fun process360(
        @Part("auth_key") authKey: RequestBody,
        @Part("type") type: RequestBody,
        @Part("project_id") projectId: RequestBody,
        @Part("sku_name") skuName: RequestBody,
        @Part("sku_id") skuId: RequestBody,
        @Part("category") category: RequestBody,
        @Part("sub_category") subCategory: RequestBody,
        @Part("frames") frames: RequestBody,
        @Part("background_id") backgroundId: RequestBody,
        @Part videoFile: MultipartBody.Part,
        @Part("video_url") videoUrl: RequestBody? = null,
    ) : ProcessThreeSixtyRes

    @FormUrlEncoded
    @POST("v3/video/video-upload")
    suspend fun getVideoPreSignedUrl(
        @Field("auth_key") authKey : String,
        @Field("project_id") projectId:String,
        @Field("sku_id") skuId : String,
        @Field("category") category : String,
        @Field("sub_category") sub_category : String,
        @Field("total_frames_no") totalFrames: Int,
        @Field("video_name") videoName : String,
        @Field("background_id") backgroundId : String? = null
    ) : VideoPreSignedRes


    @PUT
    fun uploadVideo(
        @Header("content-type") contentType: String,
        @Url uploadUrl: String,
        @Body file: RequestBody
    ): Call<ResponseBody>

    @FormUrlEncoded
    @PUT("v3/video/video-mark")
    suspend fun setStatusUploaded(
        @Field("video_id") videoId : String,
        @Field("auth_key") authKey : String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString()
    ) : VideoUploadedRes



    @GET("v2/credit/fetch")
    suspend fun userCreditsDetails(
        @Query("auth_key") userId: String
    ): CreditDetailsResponse

    @FormUrlEncoded
    @PUT("v2/credit/reduce-user-credit")
    suspend fun reduceCredit(
        @Field("auth_key") authKey : String,
        @Field("credit_reduce") creditReduce:String,
        @Field("sku_id") skuId : String,
        @Field("source") source : String = "App_android",
        @Field("image_id") imageId : String = ""
    ): ReduceCreditResponse

    @FormUrlEncoded
    @POST("v4/update-download-status")
    suspend fun updateDownloadStatus(@Field("user_id") userId : String,
                             @Field("sku_id") skuId: String,
                             @Field("enterprise_id") enterpriseId: String,
                             @Field("download_hd") downloadHd: Boolean
    ): DownloadHDRes

    @FormUrlEncoded
    @POST("v2/sku/skuProcessStatus")
    suspend fun skuProcessState(
        @Field("auth_key") auth_key: String?,
        @Field("project_id") project_id: String?
    ): SkuProcessStateResponse

    @FormUrlEncoded
    @POST("v2/sku/skuProcessStatus")
    suspend fun skuProcessStateWithBackgroundId(
        @Field("auth_key") auth_key: String?,
        @Field("project_id") project_id: String?,
        @Field("background_id") background_id: Int?,
    ): SkuProcessStateResponse

    @FormUrlEncoded
    @POST("v2/sku/skuProcessStatus")
    suspend fun skuProcessStateWithShadowOption(
        @Field("auth_key") auth_key: String?,
        @Field("project_id") project_id: String?,
        @Field("background_id") background_id: Int?,
        @Field("shadow") shadow: String?,
    ): SkuProcessStateResponse



    @FormUrlEncoded
    @POST("v4/image/image-upload-check-v2")
    suspend fun checkUploadStatus(
        @Field("auth_key") auth_key: String,
        @Field("image_name") image_name: String,
    ): UploadStatusRes

    @FormUrlEncoded
    @PATCH("v2/sku/update-iim")
    suspend fun updateFootwearSubcategory(
        @Field("auth_key") authKey: String,
        @Field("sku_id") skuId: String,
        @Field("initial_image_count") initialImageCount: Int,
        @Field("sub_cat_id") subCatId: String,
    ): UpdateFootwearSubcatRes


    @GET("v4/image/upload-folder-check")
    fun uploadFolder(@Query(
        "auth_key") authKey : String) : Call<UploadFolderRes>

    @FormUrlEncoded
    @POST("v2/image/user-data")
    suspend fun sendFilesData(
        @Field("auth_key") authKey: String,
        @Field("data") skuId: String
    ) : FilesDataRes

    @GET("v2/enterprise/compareAppVersion")
    suspend fun getVersionStatus(
        @Query("auth_key") authKey: String,
        @Query("app_version") appVersion: String
    ) : VersionStatusRes


    @GET("v2/user/project-sku-data")
    suspend fun getProjectName(
        @Query("auth_key") authKey : String,
    ): GetProjectNameResponse


    @GET("algo/save_to_gcp_presigned/presigned-url")
    suspend fun getGCPUrl(
        @Query("img_name") imageName : String,
    ) : GetGCPUrlRes

    @FormUrlEncoded
    @POST("algo/attendence/checkin-out")
    suspend fun captureCheckInOut(
        @Field("type") type : String,
        @Field("location") location : JSONObject,
        @Field("location_id") locationId: String,
        @Field("img_url") imageUrl : String = "",
        @Field("auth_key") authKey : String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString()
    ) : CheckInOutRes

    @FormUrlEncoded
    @POST("v2/image/get")
    suspend fun getImageData(
        @Field("image_id") imageId: String,
        @Field("auth_key") authKey: String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString()
    ) : GetImageRes

    @GET("algo/manual_location_data/get-data")
    suspend fun getLocations(
        @Query("auth_key") authKey : String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString()
    ) : LocationsRes



}