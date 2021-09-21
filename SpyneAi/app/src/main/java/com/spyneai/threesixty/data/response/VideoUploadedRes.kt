package com.spyneai.threesixty.data.response


import com.google.gson.annotations.SerializedName

data class VideoUploadedRes(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
)