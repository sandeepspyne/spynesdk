package com.spyneai.model.shoot

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class CreateCollectionResponse (
        val header : Header,
        val msgInfo : MsgInfo,
        val payload : Payload
)