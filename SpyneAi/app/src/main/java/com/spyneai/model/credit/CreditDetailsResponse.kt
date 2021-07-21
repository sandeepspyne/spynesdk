package com.spyneai.model.credit

import com.google.gson.annotations.SerializedName

data class CreditDetailsResponse (
    @SerializedName("status") val status : String,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : Data
        )
{
    data class Data(
        @SerializedName("credit_allotted") val credit_allotted : Int,
        @SerializedName("credit_available") val credit_available : Int,
        @SerializedName("credit_used") val credit_used : Int
    )
}