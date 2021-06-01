package com.spyneai.model.otp

import com.google.gson.annotations.SerializedName

data class SendEmailResponse(@SerializedName("id")
            val id : String,
            @SerializedName("status")
            val status : String,
            @SerializedName("message")
            val message : String,
            @SerializedName("data")
            val data : String)