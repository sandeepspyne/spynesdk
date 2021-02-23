package com.spyneai.model.skumap

import com.spyneai.model.skumap.Payload
import com.spyneai.model.skumap.Header
import com.spyneai.model.skumap.MsgInfo

data class UpdateSkuResponse (
        val header : Header,
        val msgInfo : MsgInfo,
        val payload : Payload
)