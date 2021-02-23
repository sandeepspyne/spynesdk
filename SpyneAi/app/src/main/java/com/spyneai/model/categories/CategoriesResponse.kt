package com.spyneai.model.categories

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo
import com.spyneai.model.categories.Payload

data class CategoriesResponse (
    val header : Header,
    val msgInfo : MsgInfo,
    val payload : Payload

)