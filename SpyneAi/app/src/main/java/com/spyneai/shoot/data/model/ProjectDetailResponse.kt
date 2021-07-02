package com.spyneai.shoot.data.model

import com.google.gson.annotations.SerializedName

data class ProjectDetailResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Data
) {
    data class Data(

        @SerializedName("project_name") val project_name: String,
        @SerializedName("project_id") val project_id: String,
        @SerializedName("total_sku") val total_sku: Int,
        @SerializedName("total_images") val total_images: Int,
        @SerializedName("sku") val sku: List<Sku>
    )


    data class Sku(

        @SerializedName("sku_id") val sku_id: String,
        @SerializedName("sku_name") val sku_name: String,
        @SerializedName("total_images") val total_images: Int,
        @SerializedName("images") val images: List<Images>
    )

    data class Images(
        @SerializedName("input_hres") val input_hres: String,
        @SerializedName("input_lres") val input_lres: String,
        @SerializedName("output_hres") val output_hres: String,
        @SerializedName("output_lres") val output_lres: String,
        @SerializedName("output_wm") val output_wm: String
    )
}