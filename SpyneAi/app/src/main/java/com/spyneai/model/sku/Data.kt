package com.spyneai.model.sku

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("shootId")
    val shootId : String,
    @SerializedName("skuId")
    val skuId : String,
    @SerializedName("userTokenId")
    val userTokenId : String,
    @SerializedName("displayName")
    val displayName : String,
    @SerializedName("complete")
    val complete : Boolean,
    @SerializedName("photos")
    val photos : List<Photos>,
    @SerializedName("photosCount")
    val photosCount : Int,
    @SerializedName("price")
    val price : Int
)
