package com.spyneai.shoot.response


import com.google.gson.annotations.SerializedName

data class UpdateVideoSkuRes(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
)