package com.spyneai.model.order

import com.google.gson.annotations.SerializedName

data class Sku(
        @SerializedName("shootId")
        val shootId: String,
        @SerializedName("skuId")
        val skuId: String,
        @SerializedName("userTokenId")
        val userTokenId: String,
        @SerializedName("displayName")
        val displayName: String,
        @SerializedName("complete")
        val complete: Boolean,
        @SerializedName("photos")
        val photos: List<Photo>,
        @SerializedName("photosCount")
        val photosCount: Long,
        @SerializedName("price")
        val price: Long
)
