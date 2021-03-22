package com.spyneai.model.ordersummary

import com.google.gson.annotations.SerializedName
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class OrderSummaryResponse(
        @SerializedName("header")
        val header : Header,
        @SerializedName("msgInfo")
        val msgInfo : MsgInfo,
        @SerializedName("payload")
        val payload : Payload
)
