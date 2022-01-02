package com.spyneai.dashboard.repository.model.category

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.spyneai.dashboard.response.NewCategoriesResponse

data class DynamicLayout(
    @PrimaryKey
    val categoryId : Int,
    @SerializedName("project_dialog") val project_dialog : List<NewCategoriesResponse.Project_dialog>? = null
)
