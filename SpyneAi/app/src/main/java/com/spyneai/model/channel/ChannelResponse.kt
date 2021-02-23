package com.spyneai.model.channel

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class ChannelResponse (
    val header : Header,
    val msgInfo : MsgInfo,
    val payload : Payload
)