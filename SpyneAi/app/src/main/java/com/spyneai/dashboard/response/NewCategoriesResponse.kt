package com.spyneai.dashboard.response

import com.google.gson.annotations.SerializedName

data class NewCategoriesResponse(
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : List<Data>
){
    data class Data (
        @SerializedName("id") val id : Int,
        @SerializedName("prod_cat_id") val prod_cat_id : String,
        @SerializedName("enterprise_id") val enterprise_id : String,
        @SerializedName("prod_cat_name") val prod_cat_name : String,
        @SerializedName("display_thumbnail") val display_thumbnail : String,
        @SerializedName("color_code") val color_code : String,
        @SerializedName("description") val description : String,
        @SerializedName("active") val active : Int,
        @SerializedName("priority") val priority : Int,
        @SerializedName("created_at") val created_at : String,
        @SerializedName("updated_at") val updated_at : String,
        @SerializedName("dynamic_layout") val dynamic_layout : Dynamic_layout
    )

    data class Dynamic_layout (
        @SerializedName("project_dialog") val project_dialog : List<Project_dialog>
    )

    data class Project_dialog (
        @SerializedName("field_id") val field_id : String,
        @SerializedName("field_name") val field_name : String,
        @SerializedName("hint") val hint : String,
        @SerializedName("field_type") val field_type : String,
        @SerializedName("is_required") val is_required : Boolean,
        @SerializedName("input_type") val input_type : String,
        @SerializedName("allow_special_char") val allow_special_char : Boolean,
        @SerializedName("all_caps") val all_caps : Boolean,
        @SerializedName("defult_value") val defult_value : String,
        @SerializedName("enum_values") val enum_values : List<String>
    )

}