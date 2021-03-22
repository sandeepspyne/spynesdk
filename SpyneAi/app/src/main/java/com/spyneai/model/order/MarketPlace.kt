package com.spyneai.model.order

import com.google.gson.annotations.SerializedName

data class MarketPlace(
        @SerializedName("markId")
        val markId: String,
        @SerializedName("displayName")
        val displayName: String,
        @SerializedName("displayThumbnail")
        val displayThumbnail: Any
)
