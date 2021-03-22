package com.spyneai.model.ai

import com.google.gson.annotations.SerializedName

data class GifsResponse (
        @SerializedName("Status")
        val Status : String,
        @SerializedName("gif")
        val gif : String
)