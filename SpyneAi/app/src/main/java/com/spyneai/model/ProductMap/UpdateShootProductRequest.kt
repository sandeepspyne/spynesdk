package com.spyneai.model.shoot

import com.google.gson.annotations.SerializedName

data class UpdateShootProductRequest(
        @SerializedName("shootId")
        val shootId: String,
        @SerializedName("productId")
        val productId: String,
        @SerializedName("productName")
        val productName: String
)