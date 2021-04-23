package com.spyneai.videorecording.model

import com.google.gson.annotations.SerializedName

data class UploadVideoResponse(
    @SerializedName("message")
    val message : String,
    @SerializedName("status")
    val status : String,
    @SerializedName("uploading_time")
    val uploading_time : String,
    @SerializedName("video_url")
    val video_url : String
)
