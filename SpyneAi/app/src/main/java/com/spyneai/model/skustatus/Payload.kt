package com.spyneai.model.skustatus

import com.google.gson.annotations.SerializedName
import com.spyneai.model.sku.Data

data class Payload(
        @SerializedName("data")
        val data : Data
)
