package com.spyneai.model.dashboard

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class DashboardResponse(
        val header : Header,
        val msgInfo : MsgInfo,
        val payload : Payload
)
