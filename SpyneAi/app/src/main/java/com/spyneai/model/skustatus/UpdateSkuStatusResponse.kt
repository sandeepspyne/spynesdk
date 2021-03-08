package com.spyneai.model.skustatus

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo
import com.spyneai.model.skustatus.Payload

data class UpdateSkuStatusResponse (
        val header: Header,
        val msgInfo: MsgInfo,
        val payload: Payload
        )