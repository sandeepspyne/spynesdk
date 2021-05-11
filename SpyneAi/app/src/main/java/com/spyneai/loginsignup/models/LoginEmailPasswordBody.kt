package com.spyneai.loginsignup.models

import com.google.gson.annotations.SerializedName

data class LoginEmailPasswordBody (

    @SerializedName("username") val username : String,
    @SerializedName("password") val password : String

        )