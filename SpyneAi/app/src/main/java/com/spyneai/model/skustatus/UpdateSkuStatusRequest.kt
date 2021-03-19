package com.spyneai.model.skustatus

import com.google.gson.annotations.SerializedName

data class UpdateSkuStatusRequest (
        @SerializedName("skuId")
        val skuId : String,
        @SerializedName("shootId")
        val shootId : String,
        @SerializedName("complete")
        val complete : Boolean
)