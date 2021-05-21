package com.spyneai.dashboard.data.model

import com.google.gson.annotations.SerializedName

data class SliderModel(
    @SerializedName("before")
    val before: Int,
    @SerializedName("after")
    val after: Int
)