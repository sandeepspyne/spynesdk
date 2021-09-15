package com.spyneai.db


import com.google.gson.annotations.SerializedName

data class Test(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("interior")
    val interior: List<Interior>,
    @SerializedName("message")
    val message: String,
    @SerializedName("miscellaneous")
    val miscellaneous: List<Miscellaneou>,
    @SerializedName("status")
    val status: Int,
    @SerializedName("tags")
    val tags: Tags
) {
    data class Data(
        @SerializedName("active")
        val active: Int,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("display_thumbnail")
        val displayThumbnail: String,
        @SerializedName("enterprise_id")
        val enterpriseId: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("priority")
        val priority: Int,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @SerializedName("prod_sub_cat_id")
        val prodSubCatId: String,
        @SerializedName("sub_cat_name")
        val subCatName: String,
        @SerializedName("updated_at")
        val updatedAt: String
    )

    data class Interior(
        @SerializedName("display_name")
        val displayName: String,
        @SerializedName("display_thumbnail")
        val displayThumbnail: String,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @SerializedName("prod_sub_cat_id")
        val prodSubCatId: String
    )

    data class Miscellaneou(
        @SerializedName("display_name")
        val displayName: String,
        @SerializedName("display_thumbnail")
        val displayThumbnail: String,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @SerializedName("prod_sub_cat_id")
        val prodSubCatId: String
    )

    data class Tags(
        @SerializedName("Exterior")
        val exterior: List<Exterior>,
        @SerializedName("Focus Shoot")
        val focusShoot: List<FocusShoot>
    ) {
        data class Exterior(
            @SerializedName("default_value")
            val defaultValue: String,
            @SerializedName("enum_values")
            val enumValues: List<Any>,
            @SerializedName("field_name")
            val fieldName: String,
            @SerializedName("field_type")
            val fieldType: String,
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
            @SerializedName("is_required")
            val isRequired: Boolean
        )
    }
}