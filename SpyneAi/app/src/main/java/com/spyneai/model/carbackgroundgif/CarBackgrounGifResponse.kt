package com.spyneai.model.carbackgroundgif

import com.google.gson.annotations.SerializedName

data class CarBackgrounGifResponse(
    @SerializedName("imageId") val imageId: Int,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("imageCredit") val imageCredit: Int,
    @SerializedName("bgName") val bgName: String,
    @SerializedName("gifUrl") val gifUrl: String
)