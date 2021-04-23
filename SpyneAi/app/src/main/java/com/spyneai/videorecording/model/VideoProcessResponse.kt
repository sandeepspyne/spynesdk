package com.spyneai.videorecording.model

import com.google.gson.annotations.SerializedName

data class VideoProcessResponse(
    @SerializedName("url")
    val url : List<List<String>>,

    @SerializedName("category")
    val category : String,

    @SerializedName("time_taken")
    val time_taken : Double,

    @SerializedName("orig_url")
    val orig_url : String
)
