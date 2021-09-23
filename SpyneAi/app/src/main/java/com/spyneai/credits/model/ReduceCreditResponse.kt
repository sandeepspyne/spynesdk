package com.spyneai.credits.model

import com.google.gson.annotations.SerializedName

data class ReduceCreditResponse(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    data class Data(
        @SerializedName("credit_allotted")
        val creditAllotted: Int,
        @SerializedName("credit_available")
        val creditAvailable: Int,
        @SerializedName("credit_used")
        val creditUsed: Int
    )
}