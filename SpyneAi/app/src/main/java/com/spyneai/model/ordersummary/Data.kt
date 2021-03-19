package com.spyneai.model.ordersummary

import com.google.gson.annotations.SerializedName

data class Data(
        @SerializedName("shootId")
        val shootId : String,
        @SerializedName("skuCount")
        val skuCount : Int,
        @SerializedName("photosCount")
        val photosCount : Int,
        @SerializedName("amount")
        val amount : Double,
        @SerializedName("backgroundCount")
        val backgroundCount : Int,
        @SerializedName("marketplaceCount")
        val marketplaceCount : Int
)
