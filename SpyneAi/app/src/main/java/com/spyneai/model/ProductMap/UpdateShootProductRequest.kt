package com.spyneai.model.shoot

import com.google.gson.annotations.SerializedName

data class UpdateShootProductRequest(
        val shootId: String,
        val productId: String,
        val productName: String
)