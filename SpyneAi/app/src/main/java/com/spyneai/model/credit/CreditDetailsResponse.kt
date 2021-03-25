package com.spyneai.model.credit

import com.google.gson.annotations.SerializedName

data class CreditDetailsResponse (
    @SerializedName("id") val id : Int,
    @SerializedName("status") val status : String,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : Data
        )