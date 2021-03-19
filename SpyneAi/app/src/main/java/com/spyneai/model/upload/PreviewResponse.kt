package com.spyneai.model.upload

import com.google.gson.annotations.SerializedName

data class PreviewResponse(
    @SerializedName("url")
    val url : String,
    @SerializedName("category")
    val category : String,
    @SerializedName("time_taken")
    val time_taken : Double,
    @SerializedName("orig_url")
    val orig_url : String
)