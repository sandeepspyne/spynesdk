package com.spyneai.threesixty.data.model


import com.google.gson.annotations.SerializedName

data class VideoPreSignedRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    data class Data(
        @SerializedName("auth_key")
        val authKey: String,
        @SerializedName("presigned_url")
        val presignedUrl: String,
        @SerializedName("video_id")
        val videoId: String
    )
}