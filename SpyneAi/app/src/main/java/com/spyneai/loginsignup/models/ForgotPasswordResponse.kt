package com.spyneai.loginsignup.models

import com.google.gson.annotations.SerializedName

data class ForgotPasswordResponse (
    @SerializedName("code") val code : Int,
    @SerializedName("msg") val msg : String,
    @SerializedName("data") val data : String
        )