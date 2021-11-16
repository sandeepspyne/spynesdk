package com.spyneai.dashboard.data.model


import com.google.gson.annotations.SerializedName

data class CheckInOutRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    data class Data(
        @SerializedName("checkin_time")
        val checkinTime: String
    )
}