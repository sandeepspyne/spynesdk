package com.spyneai.model.shoot

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateShootCategoryRequest (
        val shootId: String,
        val categoryId: String,
        val name: String
)