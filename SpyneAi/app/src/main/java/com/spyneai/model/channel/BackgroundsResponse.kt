package com.spyneai.model.channel

import com.google.gson.annotations.SerializedName

data class BackgroundsResponse (
        @SerializedName("image_id")
        var image_id : String,
        @SerializedName("image_url")
        var image_url : String,
        @SerializedName("category")
        var category : String,
        @SerializedName("hex_code")
        var hex_code : String
)