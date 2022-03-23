package com.spyneai.sdk.model

import com.google.gson.annotations.SerializedName

data class SignupIntoSDKRes (
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : Data
) {
    data class Data(

        @SerializedName("auth_key") val auth_auth_key: String,
        @SerializedName("secret_key") val secretKey: String
    )
}