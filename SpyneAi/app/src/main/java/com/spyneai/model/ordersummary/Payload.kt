package com.spyneai.model.ordersummary

import com.google.gson.annotations.SerializedName

data class Payload (
        @SerializedName("data")
        val data: Data
        )