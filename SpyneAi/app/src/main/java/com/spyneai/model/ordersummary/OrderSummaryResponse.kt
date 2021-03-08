package com.spyneai.model.ordersummary

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class OrderSummaryResponse(
        val header : Header,
        val msgInfo : MsgInfo,
        val payload : Payload
)
