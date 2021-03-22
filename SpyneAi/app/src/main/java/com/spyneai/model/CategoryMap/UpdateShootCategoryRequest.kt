package com.spyneai.model.shoot

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateShootCategoryRequest (
        @SerializedName("shootId")
        val shootId: String,
        @SerializedName("categoryId")
        val categoryId: String,
        @SerializedName("name")
        val name: String
)