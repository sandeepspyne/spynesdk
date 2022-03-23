package com.spyneai.shoot.repository.model.project

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity
data class Project(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("local_id")
    var uuid: String,
    @SerializedName("project_name") var projectName: String? = null,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("category") val categoryName: String? = null,
    @SerializedName("project_id") var projectId: String? = null,
    @SerializedName("dynamic_layout") val dynamicLayout: String? = null,
    @SerializedName("location_data") val locationData: String? = null,
    @SerializedName("status") var status: String = "draft",
    @SerializedName("rating") var rating: String? = null,
    @SerializedName("total_sku") var skuCount: Int = 0,
    @SerializedName("total_images") var imagesCount: Int = 0,
    @SerializedName("processed_images") var processedCount: Int = 0,
    @SerializedName("thumbnail") var thumbnail: String? = null,
    var isCreated: Boolean = false,
    val toProcessAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 1,
    @SerializedName("created_on") var createdOn: String = System.currentTimeMillis().toString(),
    @SerializedName("created_at") var createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)
