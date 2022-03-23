package com.spyneai.shoot.repository.model.project


import com.google.gson.annotations.SerializedName
import com.spyneai.BaseApplication
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

data class ProjectBody(
    @SerializedName("auth_key")
    val authKey: String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY)!!,
    @SerializedName("project_data")
    val projectData: ProjectData,
    @SerializedName("sku_data")
    val skuData: List<SkuData>
) {
    data class ProjectData(
        @SerializedName("category_id")
        val categoryId: String,
        @SerializedName("dynamic_layout")
        var dynamicLayout: DynamicLayout? = null,
        @SerializedName("local_id")
        val localId: String,
        @SerializedName("project_id")
        val projectId: String?,
        @SerializedName("location_data")
        var locationData: LocationData? = null,
        @SerializedName("project_name")
        val projectName: String,
        @SerializedName("foreign_sku_id")
        val foreignSkuId: String? = null,
        @SerializedName("source")
        val source: String = "App_android"
    ) {
        data class DynamicLayout(
            @SerializedName("dynamic")
            val `dynamic`: String? = null
        )

        data class LocationData(
            @SerializedName("location")
            val location: String? = null
        )
    }

    data class SkuData(
        @SerializedName("sku_id")
        val skuId: String?,
        @SerializedName("image_present")
        val imagePresent: Int,
        @SerializedName("initial_no")
        val initialNo: Int,
        @SerializedName("local_id")
        val localId: String,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @SerializedName("prod_sub_cat_id")
        val prodSubCatId: String? = null,
        @SerializedName("sku_name")
        val skuName: String,
        @SerializedName("source")
        val source: String = "App_android",
        @SerializedName("total_frames_no")
        val totalFramesNo: Int,
        @SerializedName("video_present")
        val videoPresent: Int
    )
}