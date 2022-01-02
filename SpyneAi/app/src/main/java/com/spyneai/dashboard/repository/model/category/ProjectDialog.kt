package com.spyneai.dashboard.repository.model.category

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class ProjectDialog(
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
