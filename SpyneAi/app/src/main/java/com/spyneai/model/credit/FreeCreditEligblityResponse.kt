package com.spyneai.model.credit

import com.google.gson.annotations.SerializedName

data class FreeCreditEligblityResponse(
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String
)