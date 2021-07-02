package com.spyneai.shoot.data.model

import com.google.gson.annotations.SerializedName

data class ProjectDetailResponse(
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : Data
) {
    data class Data (

        @SerializedName("total_projects") val total_projects : Int,
        @SerializedName("total_images") val total_images : Int,
        @SerializedName("total_skus") val total_skus : Int,
        @SerializedName("project_data") val project_data : List<Project_data>
    )


    data class Project_data (

        @SerializedName("project_name") val project_name : String,
        @SerializedName("project_id") val project_id : String,
        @SerializedName("total_sku") val total_sku : Int,
        @SerializedName("sku") val sku : List<Sku>
    )

    data class Sku (

        @SerializedName("sku_id") val sku_id : String,
        @SerializedName("sku_name") val sku_name : String,
        @SerializedName("total_images") val total_images : Int,
        @SerializedName("images") val images : List<String>
    )
}