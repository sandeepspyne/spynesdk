package com.spyneai.model.login

import com.google.gson.annotations.SerializedName

data class LoginResponse (
        @SerializedName("header")
        val header : Header,
        @SerializedName("msgInfo")
        val msgInfo : MsgInfo,
        @SerializedName("payload")
        val payload : Payload
)