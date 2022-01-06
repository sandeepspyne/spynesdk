package com.spyneai.shoot.repository.model.project

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Project(
    @PrimaryKey(autoGenerate = false)
    val uuid: String,
    var isCreated: Boolean = false,
    val toProcessAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    @SerializedName("project_name") var projectName: String? = null,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("subcategory_name") var subCategoryName : String? = null,
    @SerializedName("subcategory_id")var subCategoryId : String? = null,
    @SerializedName("project_id") val projectId: String? = null,
    @SerializedName("dynamic_layout") val dynamicLayout: String? = null,
    @SerializedName("location_data") val locationData: String? = null,
    @SerializedName("status") var status: String = "draft",
    @SerializedName("rating") var rating: String? = null,
    @SerializedName("sku_count") var skuCount: Int = 0,
    @SerializedName("images_count") var imagesCount: Int = 0,
    @SerializedName("thumbnail") var thumbnail: String? = null,
    @SerializedName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)
