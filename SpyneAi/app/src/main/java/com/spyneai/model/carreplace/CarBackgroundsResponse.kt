package com.spyneai.model.carreplace

import com.google.gson.annotations.SerializedName

data class CarBackgroundsResponse(
        @SerializedName("imageId")

        val imageId : Int,
        @SerializedName("imageUrl")

        val imageUrl : String,
        @SerializedName("imageCredit")

        val imageCredit : Int,
        @SerializedName("bgName")

        val bgName : String
)