package com.spyneai.model.marketupdate

import com.google.gson.annotations.SerializedName

data class MarketPlace(
        @SerializedName("markId")
        val markId: String,
        @SerializedName("displayName")
        val displayName: String
        )
