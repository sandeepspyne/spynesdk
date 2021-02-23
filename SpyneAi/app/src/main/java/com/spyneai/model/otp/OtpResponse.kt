package com.spyneai.model.otp

import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class OtpResponse (
        val header : Header,
        val msgInfo : MsgInfo,
        val payload : String
)