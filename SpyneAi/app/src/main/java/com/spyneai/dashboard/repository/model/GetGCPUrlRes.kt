package com.spyneai.dashboard.repository.model


import com.google.gson.annotations.SerializedName

data class GetGCPUrlRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    data class Data(
        @SerializedName("file_url")
        val fileUrl: String,
        @SerializedName("presigned_url")
        val presignedUrl: String
    )
}