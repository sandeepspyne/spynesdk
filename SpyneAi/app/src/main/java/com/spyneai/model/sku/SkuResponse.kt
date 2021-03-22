package com.spyneai.model.sku

import com.google.gson.annotations.SerializedName
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class SkuResponse (
    @SerializedName("header")
    val header : Header,
    @SerializedName("msgInfo")
    val msgInfo : MsgInfo,
    @SerializedName("payload")
    val payload:Payload
        )