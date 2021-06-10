package com.spyneai.shoot.data.model

import com.google.gson.annotations.SerializedName

data class UploadImageResponse (
    @SerializedName("input_image_hres_url") val input_image_hres_url : String,
    @SerializedName("image_id") val image_id : String,
    @SerializedName("image_name") val image_name : String,
    @SerializedName("category") val category : String,
    @SerializedName("status") val status : String,
    @SerializedName("output_image_hres_url") val output_image_hres_url : String,
    @SerializedName("output_image_lres_url") val output_image_lres_url : String
        )