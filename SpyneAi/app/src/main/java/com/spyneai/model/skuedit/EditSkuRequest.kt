package com.spyneai.model.skuedit

import com.google.gson.annotations.SerializedName

data class EditSkuRequest(
        @SerializedName("skuId")
        val skuId : String,
        @SerializedName("displayName")
        val displayName : String
)