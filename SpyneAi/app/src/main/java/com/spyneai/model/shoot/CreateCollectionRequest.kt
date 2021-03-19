package com.spyneai.model.shoot

import com.google.gson.annotations.SerializedName

data class CreateCollectionRequest (
    @SerializedName("shootName")
    val  shootName : String
)