package com.spyneai.loginsignup.models

import com.google.gson.annotations.SerializedName

data class SignupResponse (
    @SerializedName("auth_token") val auth_token : String,
    @SerializedName("message") val message : String,
    @SerializedName("status") val status : Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName : String,
    @SerializedName("email_id") val emailId : String
        )