package com.spyneai.model.ai

import com.google.gson.annotations.SerializedName

data class GifFetchResponse(
        @SerializedName("id")
        val id : Int,
        @SerializedName("user_id")
        val user_id : String,
        @SerializedName("sku_id")
        val sku_id : String,
        @SerializedName("gif_url")
        val gif_url : String
)