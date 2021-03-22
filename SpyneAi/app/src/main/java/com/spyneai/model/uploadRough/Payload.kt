package com.spyneai.model.uploadRough

import com.google.gson.annotations.SerializedName

data class Payload (
        @SerializedName("data")
        val data : Data
        )