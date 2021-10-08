package com.spyneai.shoot.data.model


import com.google.gson.annotations.SerializedName

data class ImagePreSignedRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    data class Data(
        @SerializedName("image_id")
        val imageId: String,
        @SerializedName("presigned_url")
        val presignedUrl: String
    )
}