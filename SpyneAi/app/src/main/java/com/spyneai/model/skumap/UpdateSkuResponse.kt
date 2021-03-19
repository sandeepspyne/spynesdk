package com.spyneai.model.skumap

import com.google.gson.annotations.SerializedName
import com.spyneai.model.skumap.Payload
import com.spyneai.model.skumap.Header
import com.spyneai.model.skumap.MsgInfo

data class UpdateSkuResponse (
        @SerializedName("header")
        val header : Header,
        @SerializedName("msgInfo")
        val msgInfo : MsgInfo,
        @SerializedName("payload")
        val payload : Payload
)