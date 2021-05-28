package com.spyneai.model.otp

import com.google.gson.annotations.SerializedName
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class OtpResponse (
        @SerializedName("id")
        val id : String,
        @SerializedName("status")
        val status : String,
        @SerializedName("message")
        val message : String,
        @SerializedName("data")
        val data : String,
        @SerializedName("user_name") val user_name : String,
        @SerializedName("email_id") val email_id : String
)