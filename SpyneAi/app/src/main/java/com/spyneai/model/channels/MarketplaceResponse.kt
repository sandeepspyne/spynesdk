package com.spyneai.model.channels

import com.spyneai.model.channels.Payload
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class MarketplaceResponse(
    val header : Header,
    val msgInfo : MsgInfo,
    val payload : Payload
)
