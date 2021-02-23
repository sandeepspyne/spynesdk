package com.spyneai.model.order

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Sku(
        val shootId: String,
        val skuId: String,
        val userTokenId: String,

        val displayName: String,

        val complete: Boolean,

        val photos: List<Photo>,

        val photosCount: Long,

        val price: Long
)
