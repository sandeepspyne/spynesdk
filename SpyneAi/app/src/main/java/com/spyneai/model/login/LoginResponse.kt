package com.spyneai.model.login

import com.google.gson.annotations.SerializedName

data class LoginResponse (
        @SerializedName("message")
        val message : String,
        @SerializedName("status")
        val status : Int,
        @SerializedName("user_id")
        val userId : String
)