package com.spyneai.model.carreplace

import com.google.gson.annotations.SerializedName

data class AddCarLogoResponse(
    @SerializedName("message")
    val message : String,
    @SerializedName("org_url")
    val org_url : String,
    @SerializedName("status")
    val status : Int
)
