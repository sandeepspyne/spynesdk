package com.spyneai.model.credit

import com.google.gson.annotations.SerializedName

data class Data (
    @SerializedName("userId") val userId : String,
    @SerializedName("creditId") val creditId : String,
    @SerializedName("creditAlloted") val creditAlloted : Int,
    @SerializedName("creditUsed") val creditUsed : Int,
    @SerializedName("creditAvailable") val creditAvailable : Int
        )