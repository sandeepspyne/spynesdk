package com.spyneai.model.skustatus

data class UpdateSkuStatusRequest (
        val skuId : String,
        val shootId : String,
        val complete : Boolean
)