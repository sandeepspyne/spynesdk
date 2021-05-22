package com.spyneai.dashboard.response

import com.google.gson.annotations.SerializedName
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo
import com.spyneai.model.shoot.Payload

data class UpdateShootResponse (
    @SerializedName("header")
    val header : Header,
    @SerializedName("msgInfo")
    val msgInfo : MsgInfo,
    @SerializedName("payload")
    val payload : Payload
        )