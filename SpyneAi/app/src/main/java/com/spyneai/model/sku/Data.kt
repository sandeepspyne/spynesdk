package com.spyneai.model.sku

data class Data(
    val shootId : String,
    val skuId : String,
    val userTokenId : String,
    val displayName : String,
    val complete : Boolean,
    val photos : List<Photos>,
    val photosCount : Int,
    val price : Int
)
