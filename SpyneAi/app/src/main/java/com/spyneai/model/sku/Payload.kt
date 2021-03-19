package com.spyneai.model.sku

import com.google.gson.annotations.SerializedName

data class Payload(
    @SerializedName("data")
    val data : Data
)
