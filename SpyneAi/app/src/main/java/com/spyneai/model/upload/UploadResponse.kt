package com.spyneai.model.upload

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("image")
    val image : String
)