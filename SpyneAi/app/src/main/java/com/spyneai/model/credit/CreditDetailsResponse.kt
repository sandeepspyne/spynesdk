package com.spyneai.model.credit

import com.google.gson.annotations.SerializedName

data class CreditDetailsResponse(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        @SerializedName("credit_allotted")
        val creditAlloted: Int,
        @SerializedName("credit_available")
        val creditAvailable: Int,
        @SerializedName("credit_used")
        val creditUsed: Int
    )
}