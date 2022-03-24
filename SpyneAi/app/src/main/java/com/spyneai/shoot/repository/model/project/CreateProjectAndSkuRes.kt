package com.spyneai.shoot.repository.model.project


import com.google.gson.annotations.SerializedName

data class CreateProjectAndSkuRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("project_id")
        val projectId: String,
        @SerializedName("draftAvailable")
        val draftAvailable: Boolean,
        @SerializedName("draftData")
        val draftData: List<DraftData>,
        @SerializedName("skusList")
        val skusList: List<Skus>
    ) {
        data class DraftData(
            @SerializedName("category_id")
            val categoryId: String,
            @SerializedName("created_on")
            val createdOn: String,
            @SerializedName("display_image")
            val displayImage: String,
            @SerializedName("exterior_click")
            val exteriorClick: Int,
            @SerializedName("imageList")
            val imageList: List<Image>,
            @SerializedName("is_360")
            val is360: String,
            @SerializedName("local_id")
            val localId: String,
            @SerializedName("processed_images")
            val processedImages: Int,
            @SerializedName("sku_id")
            val skuId: String,
            @SerializedName("sku_name")
            val skuName: String,
            @SerializedName("status")
            val status: String,
            @SerializedName("sub_category")
            val subCategory: String,
            @SerializedName("sub_category_id")
            val subCategoryId: String,
            @SerializedName("total_frames_no")
            val totalFramesNo: Int,
            @SerializedName("total_images_captured")
            val totalImagesCaptured: Int
        ) {
            data class Image(
                @SerializedName("background_id")
                val backgroundId: String,
                @SerializedName("created_on")
                val createdOn: String,
                @SerializedName("enterprise_id")
                val enterpriseId: String,
                @SerializedName("image_category")
                val imageCategory: String,
                @SerializedName("image_id")
                val imageId: String,
                @SerializedName("image_name")
                val imageName: String,
                @SerializedName("input_image_hres_url")
                val inputImageHresUrl: String,
                @SerializedName("input_image_lres_url")
                val inputImageLresUrl: String,
                @SerializedName("output_image_hres_url")
                val outputImageHresUrl: String,
                @SerializedName("output_image_lres_url")
                val outputImageLresUrl: String,
                @SerializedName("output_image_lres_wm_url")
                val outputImageLresWmUrl: String,
                @SerializedName("prod_cat_name")
                val prodCatName: String,
                @SerializedName("project_id")
                val projectId: String,
                @SerializedName("sku_id")
                val skuId: String,
                @SerializedName("source")
                val source: String,
                @SerializedName("status")
                val status: String,
                @SerializedName("tags")
                val tags: String,
                @SerializedName("updated_on")
                val updatedOn: String,
                @SerializedName("user_id")
                val userId: String
            )
        }
        data class Skus(
            @SerializedName("local_id")
            val localId: String,
            @SerializedName("sku_id")
            val skuId: String,
            @SerializedName("sku_name")
            val skuName: String
        )
    }
}