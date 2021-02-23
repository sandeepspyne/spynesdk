package com.spyneai.model.shoot

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateSkuRequest (
        val shootId: String,
        val productId: String
        )