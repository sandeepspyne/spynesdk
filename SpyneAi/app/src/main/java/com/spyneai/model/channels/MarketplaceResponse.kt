package com.spyneai.model.channels

import com.google.gson.annotations.SerializedName
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class MarketplaceResponse(
    @SerializedName("header")
    val header : Header,
    @SerializedName("msgInfo")
    val msgInfo : MsgInfo,
    @SerializedName("payload")
    val payload : Payload
)
