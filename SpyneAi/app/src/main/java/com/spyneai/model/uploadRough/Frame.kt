package com.spyneai.model.uploadRough

import com.google.gson.annotations.SerializedName

data class Frame(
        @SerializedName("displayImage")
        val displayImage : String,
        @SerializedName("frameNumber")
        val frameNumber : Int
)