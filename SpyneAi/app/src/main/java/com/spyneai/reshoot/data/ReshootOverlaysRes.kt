package com.spyneai.reshoot.data


import com.google.gson.annotations.SerializedName

data class ReshootOverlaysRes(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    data class Data(
        @SerializedName("active")
        val active: Int,
        @SerializedName("angle_name")
        val angleName: String,
        @SerializedName("angles")
        val angles: Int,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("display_name")
        val displayName: String,
        @SerializedName("display_thumbnail")
        val displayThumbnail: String,
        @SerializedName("enterprise_id")
        val enterpriseId: String,
        @SerializedName("frame_angle")
        val frameAngle: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("overlay_id")
        val overlayId: String,
        @SerializedName("priority")
        val priority: Int,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @SerializedName("prod_sub_cat_id")
        val prodSubCatId: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("updated_at")
        val updatedAt: String,
        var isSelected : Boolean = false,
        var imageClicked : Boolean = false,
        var imagePath : String
    )
}