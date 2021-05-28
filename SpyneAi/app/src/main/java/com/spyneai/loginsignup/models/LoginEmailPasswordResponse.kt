package com.spyneai.loginsignup.models

import com.google.gson.annotations.SerializedName

data class LoginEmailPasswordResponse (
    @SerializedName("auth_token") val auth_token : String,
    @SerializedName("message") val message : String,
    @SerializedName("user_id") val user_id : String,
    @SerializedName("status") val status : Int,
    @SerializedName("user_name") val user_name : String,
    @SerializedName("email_id") val email_id : String
        )