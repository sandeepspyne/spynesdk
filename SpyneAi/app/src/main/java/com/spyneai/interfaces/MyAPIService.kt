package com.spyneai.interfaces

import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.loginsignup.models.ForgotPasswordResponse
import com.spyneai.loginsignup.models.GetCountriesResponse
import com.spyneai.loginsignup.models.LoginEmailPasswordResponse
import com.spyneai.loginsignup.models.SignupResponse
import com.spyneai.model.login.LoginRequest
import com.spyneai.model.login.LoginResponse
import com.spyneai.model.otp.OtpResponse
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.needs.AppConstants
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface MyAPIService {

    @FormUrlEncoded
    @POST("v2/user/login")
    fun loginEmailPassword(@Field("email_id") email_id : String,
                           @Field("api_key") apiKey : String,
                           @Field("password") password : String,
                           @Field("strategy") strategy : String):
            Call<LoginEmailPasswordResponse>?

    @GET("v2/user/countries")
    fun getCountries() : Call<GetCountriesResponse>


    @FormUrlEncoded
    @POST("v2/user/signup")
    fun signUp(
        @Field("api_key") apiKey: String,
        @Field("email_id") email_id: String,
        @Field("password") password: String,
        @Field("strategy") strategy: String,
        @Field("first_name") user_name: String,
        @Field("country") country: String,
        @Field("source") source: String,
        @Field("coupon_code") couponCode: String
    ):

            Call<SignupResponse>?


    @GET("v2/user/forgot-password")
    fun forgotPassword(
        @Query("email_id") userId: String?,
        @Query("api_key") apiKey: String = WhiteLabelConstants.API_KEY
    ): Call<ForgotPasswordResponse>?

    @POST("user/phone")
    fun loginApp(@Body loginRequest: LoginRequest?): Call<LoginResponse>?

    /* @POST("user/signin")
     fun postOtp(@Header("tokenId") tokenId: String?, @Body userOtp: OtpRequest): Call<OtpResponse>?
 */
    @FormUrlEncoded
    @POST("v2/user/validate-otp")
    fun postOtp(@Field("email_id") email_id : String,
                @Field("api_key") apiKey : String,
                @Field("otp") otp : String,
        @Field("source") source : String):
            Call<OtpResponse>?

    @FormUrlEncoded
    @POST("v2/user/request-otp")
    fun loginEmailApp(@Field("email_id") email_id : String,
                      @Field("api_key") apiKey : String):
            Call<LoginResponse>?


    @GET("v2/product/fetch")
    fun getCategories(@Query("auth_key") authKey : String): Call<NewCategoriesResponse>?


    @GET("v2/prod/sub/fetch")
    fun getSubCategories(
        @Query("auth_key") authKey : String,
        @Query("prod_id") prodId : String
    ): Call<NewSubCatResponse>?



    @Multipart
    @POST("fetch-sku-name")
    fun getCompletedProjects(@Part("user_id") user_id: RequestBody?)
            : Call<List<CompletedProjectResponse>>?

}