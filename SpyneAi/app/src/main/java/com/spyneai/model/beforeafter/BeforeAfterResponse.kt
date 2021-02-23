package com.spyneai.model.beforeafter

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class BeforeAfterResponse(
        val header : Header,
        val msgInfo : MsgInfo,
        val payload : Payload
)
