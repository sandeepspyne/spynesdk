package com.spyneai.model.skumap

import com.google.gson.annotations.SerializedName

data class UpdateSkuResponse (
        @SerializedName("header")
        val header : Header,
        @SerializedName("msgInfo")
        val msgInfo : MsgInfo,
        @SerializedName("payload")
        val payload : Payload
)