package com.spyneai.loginsignup.models

import com.google.gson.annotations.SerializedName

data class SignupResponse (
    @SerializedName("email") val email : String,
    @SerializedName("userId") val userId : String,
    @SerializedName("fbUserId") val fbUserId : String,
    @SerializedName("token") val token : String,
    @SerializedName("fbToken") val fbToken : String,
    @SerializedName("status") val status : String,
    @SerializedName("userName") val userName : String,
    @SerializedName("firstName") val firstName : String,
    @SerializedName("lastName") val lastName : String,
    @SerializedName("isAgent") val isAgent : Int,
    @SerializedName("userType") val userType : String,
    @SerializedName("message") val message : String,
    @SerializedName("roles") val roles : String,
    @SerializedName("imgUrl") val imgUrl : String,
    @SerializedName("vendorType") val vendorType : String,
    @SerializedName("vendorId") val vendorId : String,
    @SerializedName("productSubscribed") val productSubscribed : String,
    @SerializedName("redirectUrl") val redirectUrl : String,
    @SerializedName("statusCode") val statusCode : Int,
    @SerializedName("newUser") val newUser : Boolean,
    @SerializedName("contactNumber") val contactNumber : String,
    @SerializedName("exists") val exists : Boolean
        )