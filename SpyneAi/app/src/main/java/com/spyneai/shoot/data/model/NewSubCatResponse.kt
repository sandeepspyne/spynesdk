package com.spyneai.dashboard.response

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName



data class NewSubCatResponse(
    val `data`: List<Subcategory>?,
    val interior: List<Interior>?,
    val message: String,
    var miscellaneous: List<Miscellaneous>?,
    val status: Int,
    @SerializedName("tags")
    val tags: Tags?
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
        @SerializedName("id")
        val overlayId : Int,
        val display_name: String,
        val display_thumbnail: String,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @PrimaryKey
        @SerializedName("prod_sub_cat_id")
        val prodSubCatId: String,
        var isSelected : Boolean = false,
        var imageClicked : Boolean = false,
        var imagePath : String,
        var sequenceNumber : Int
    )

    @Entity
    data class Miscellaneous(
        @SerializedName("id")
        val overlayId : Int,
        val display_name: String,
        val display_thumbnail: String,
        val prod_cat_id: String,
        @PrimaryKey
        val prod_sub_cat_id: String,
        var isSelected : Boolean = false,
        var imageClicked : Boolean = false,
        var imagePath : String,
        var sequenceNumber : Int
    )

    data class Tags(
        @SerializedName("Exterior")
        val exterior: List<Exterior>,
        @SerializedName("Focus Shoot")
        val focusShoot: List<FocusShoot>,
        @SerializedName("Interior")
        val interior: List<Interior>
    ) {
        data class Exterior(
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

        data class FocusShoot(
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

        data class Interior(
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