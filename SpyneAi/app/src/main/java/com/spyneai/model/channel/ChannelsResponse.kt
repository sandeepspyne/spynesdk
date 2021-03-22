package com.spyneai.model.channel

import com.google.gson.annotations.SerializedName

data class ChannelsResponse (
        @SerializedName("market_id")
        var market_id : String,
        @SerializedName("image_url")
        var image_url : String,
        @SerializedName("image_credit")
        val image_credit : Int,
        @SerializedName("category")
        var category : String,
        @SerializedName("hex_code")
        var hex_code : String
)