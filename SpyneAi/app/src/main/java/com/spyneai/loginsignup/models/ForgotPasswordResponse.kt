package com.spyneai.loginsignup.models

import com.google.gson.annotations.SerializedName

data class ForgotPasswordResponse (
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    class Data
}