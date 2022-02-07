package com.spyneai.dashboard.repository.model


import com.google.gson.annotations.SerializedName

data class CheckInOutRes(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val checkout_time: String,
        val checkin_time: String
    )
}