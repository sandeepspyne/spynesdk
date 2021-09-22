package com.spyneai.orders.data.response

import com.google.gson.annotations.SerializedName

data class GetProjectsResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Data
) {
    data class Data(

        @SerializedName("total_projects") val total_projects: Int,
        @SerializedName("total_images") val total_images: Int,
        @SerializedName("total_skus") val total_skus: Int,
        @SerializedName("project_data") val project_data: List<Project_data>
    )

    data class Project_data(

        @SerializedName("project_name") val project_name : String,
        @SerializedName("project_id") val project_id : String,
        @SerializedName("total_sku") val total_sku : Int,
        @SerializedName("status") val status : String,
        @SerializedName("created_on") val created_on : String,
        @SerializedName("category") val category : String,
        @SerializedName("category_id") val categoryId : String,
        @SerializedName("sub_category") val sub_category : String,
        @SerializedName("sub_category_id") val subCategoryId : String,
        @SerializedName("processed_images") val processed_images : Int,
        @SerializedName("total_images") val total_images : Int,
        @SerializedName("sku") val sku : List<Sku>
    )

    data class Sku(
        @SerializedName("sku_id") val sku_id: String,
        @SerializedName("created_on") val created_on: String,
        @SerializedName("status") val status: String,
        @SerializedName("paid") val paid: Boolean,
        @SerializedName("sku_name") val sku_name: String,
        @SerializedName("total_frames_no") val total_frames_no: Int,
        @SerializedName("total_images") val total_images: Int,
        @SerializedName("processed_images") val processed_images: Int,
        @SerializedName("category") val category: String,
        @SerializedName("category_id") val categoryId: String,
        @SerializedName("sub_category") val subCategory: String,
        @SerializedName("sub_category_id") val subCategoryId: String,
        @SerializedName("Exterior_clicks") val exteriorClicks: Int,
        @SerializedName("images") val images: List<Images>,
        @SerializedName("is_360") val is360: Boolean,
        @SerializedName("video_id") val videoId : String,
        @SerializedName("video_url") val videoUrl : String
    )

    data class Images(

        @SerializedName("input_hres") val input_hres: String,
        @SerializedName("input_lres") val input_lres: String,
        @SerializedName("output_hres") val output_hres: String,
        @SerializedName("output_lres") val output_lres: String,
        @SerializedName("output_wm") val output_wm: String
    )
}