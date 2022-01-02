package com.spyneai.dashboard.response

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


data class NewSubCatResponse(
    val `data`: List<Subcategory>,
    var interior: List<Interior>,
    val message: String,
    var miscellaneous: List<Miscellaneous>,
    val status: Int,
    @SerializedName("tags")
    val tags: Tags
) {
    @Entity
    data class Subcategory(
        val active: Int,
        val created_at: String,
        val display_thumbnail: String,
        val enterprise_id: String,
        val id: Int,
        val priority: Int,
        val prod_cat_id: String,
        @PrimaryKey
        val prod_sub_cat_id: String,
        val sub_cat_name: String,
        val updated_at: String
    )

    @Entity
    data class Interior(
        @PrimaryKey
        @SerializedName("id")
        val overlayId: Int,
        val display_name: String,
        val display_thumbnail: String,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @SerializedName("prod_sub_cat_id")
        val prodSubCatId: String,
        var isSelected: Boolean = false,
        var imageClicked: Boolean = false,
        var imagePath: String? = null,
        var sequenceNumber: Int = 0
    )

    @Entity
    data class Miscellaneous(
        @PrimaryKey
        @SerializedName("id")
        val overlayId: Int,
        val display_name: String,
        val display_thumbnail: String,
        val prod_cat_id: String,
        val prod_sub_cat_id: String,
        var isSelected: Boolean = false,
        var imageClicked: Boolean = false,
        var imagePath: String? = null,
        var sequenceNumber: Int = 0
    )

    data class Tags(
        @SerializedName("Exterior")
        val exteriorTags: List<ExteriorTags>,
        @SerializedName("Focus Shoot")
        val focusShoot: List<FocusShoot>,
        @SerializedName("Interior")
        val interiorTags: List<InteriorTags>
    ) {
        @Entity
        data class ExteriorTags(
            @PrimaryKey(autoGenerate = true)
            val id : Int,
            @SerializedName("default_value")
            val defaultValue: String,
            @SerializedName("enum_values")
            val enumValues: List<String>,
            @SerializedName("field_name")
            val fieldName: String,
            @SerializedName("field_type")
            val fieldType: String,
            @SerializedName("field_id")
            val fieldId: String,
            @SerializedName("is_required")
            val isRequired: Boolean
        )
        @Entity
        data class FocusShoot(
            @PrimaryKey(autoGenerate = true)
            val id : Int,
            @SerializedName("default_value")
            val defaultValue: String,
            @SerializedName("enum_values")
            val enumValues: List<String>,
            @SerializedName("field_name")
            val fieldName: String,
            @SerializedName("field_type")
            val fieldType: String,
            @SerializedName("field_id")
            val fieldId: String,
            @SerializedName("is_required")
            val isRequired: Boolean
        )
        @Entity
        data class InteriorTags(
            @PrimaryKey(autoGenerate = true)
            val id : Int,
            @SerializedName("default_value")
            val defaultValue: String,
            @SerializedName("enum_values")
            val enumValues: List<String>,
            @SerializedName("field_name")
            val fieldName: String,
            @SerializedName("field_type")
            val fieldType: String,
            @SerializedName("field_id")
            val fieldId: String,
            @SerializedName("is_required")
            val isRequired: Boolean
        )
    }
}