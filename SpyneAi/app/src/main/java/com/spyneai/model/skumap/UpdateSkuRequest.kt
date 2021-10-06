package com.spyneai.model.shoot

import com.google.gson.annotations.SerializedName

data class UpdateSkuRequest (
        @SerializedName("shootId")
        val shootId: String,
        @SerializedName("productId")
        val productId: String
        )