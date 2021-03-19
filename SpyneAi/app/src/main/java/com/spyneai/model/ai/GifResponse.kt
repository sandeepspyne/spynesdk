package com.spyneai.model.ai

import com.google.gson.annotations.SerializedName

data class GifResponse (
    @SerializedName("backgroundId")
    val backgroundId : Int,
    @SerializedName("url")
    val url : String
)