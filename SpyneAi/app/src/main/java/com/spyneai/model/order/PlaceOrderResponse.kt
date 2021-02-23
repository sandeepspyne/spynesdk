package com.spyneai.model.order

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class PlaceOrderResponse(
        val header : Header,
        val msgInfo : MsgInfo,
        val payload: Payload
)
