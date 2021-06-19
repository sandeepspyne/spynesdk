package com.spyneai.model.otp

import com.google.gson.annotations.SerializedName
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class OtpResponse (
        @SerializedName("auth_token")
        val authToken : String,
        @SerializedName("email_id")
        val emailId : String,
        @SerializedName("message")
        val message : String,
        @SerializedName("status")
        val status : Int,
        @SerializedName("user_id")
        val userId : String,
        @SerializedName("user_name")
        val userName : String,
)