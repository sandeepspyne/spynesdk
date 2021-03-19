package com.spyneai.model.nextsku

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SkuRequest(
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("shootId")
    val shootId: String,
    @SerializedName("skuId")
    val skuId: String
)
