package com.spyneai.model.credit

import com.google.gson.annotations.SerializedName

class UpdateCreditResponse (
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String
)