package com.spyneai.model.nextsku

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SkuRequest(
         val displayName: String,
         val shootId: String,
         val skuId: String
)
