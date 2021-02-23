package com.spyneai.model.shoot

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class UpdateShootCategoryResponse(
        val header: Header,
        val msgInfo: MsgInfo,
        val payload: List<Any?>?
)