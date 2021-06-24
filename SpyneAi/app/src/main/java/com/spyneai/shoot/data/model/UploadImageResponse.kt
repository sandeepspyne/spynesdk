package com.spyneai.shoot.data.model

data class UploadImageResponse(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val category: String,
        val image_id: String,
        val image_name: String,
        val input_image_hres_url: String,
        val output_image_hres_url: String,
        val output_image_lres_url: String,
        val status: String
    )
}