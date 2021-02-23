package com.spyneai.model.orders

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo
import com.spyneai.model.orders.Payload

data class MyOrdersResponse (
        val header : Header,
        val msgInfo : MsgInfo,
        val payload : Payload
)