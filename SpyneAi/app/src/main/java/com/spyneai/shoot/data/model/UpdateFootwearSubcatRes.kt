package com.spyneai.shoot.data.model

import com.google.gson.annotations.SerializedName

data class UpdateFootwearSubcatRes (
    @SerializedName("message") val message : String,
    @SerializedName("status") val status : Int
)
