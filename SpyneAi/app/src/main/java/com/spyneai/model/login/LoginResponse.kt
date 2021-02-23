package com.spyneai.model.login

data class LoginResponse (
        val header : Header,
        val msgInfo : MsgInfo,
        val payload : Payload
)