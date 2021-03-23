package com.spyneai.model.marketplace

import com.spyneai.model.skumap.MsgInfo
import okhttp3.internal.http2.Header


class MarketplaceBackgroundResponse (
    val header : Header,
    val msgInfo : MsgInfo,
    val payload: Payload
)