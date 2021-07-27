package com.spyneai.model.credit

import com.google.gson.annotations.SerializedName

data class CreditDetailsResponse(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        @SerializedName("credit_allotted")
        val credit_allotted: Int,
        @SerializedName("credit_available")
        val credit_available: Int,
        @SerializedName("credit_used")
        val credit_used: Int
    )
}