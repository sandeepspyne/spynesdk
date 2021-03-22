package com.spyneai.model.order

import com.google.gson.annotations.SerializedName

data class Payload(
        @SerializedName("data")
        val data : Data
)
