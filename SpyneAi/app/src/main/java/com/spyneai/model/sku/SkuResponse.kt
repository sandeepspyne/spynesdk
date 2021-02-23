package com.spyneai.model.sku

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class SkuResponse (
    val header : Header,
    val msgInfo : MsgInfo,
    val payload:Payload
        )