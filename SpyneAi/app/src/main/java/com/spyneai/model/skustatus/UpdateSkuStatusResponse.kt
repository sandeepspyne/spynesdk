package com.spyneai.model.skustatus

import com.google.gson.annotations.SerializedName
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo
import com.spyneai.model.skustatus.Payload

data class UpdateSkuStatusResponse (
        @SerializedName("header")
        val header: Header,
        @SerializedName("msgInfo")
        val msgInfo: MsgInfo,
        @SerializedName("payload")
        val payload: Payload
)