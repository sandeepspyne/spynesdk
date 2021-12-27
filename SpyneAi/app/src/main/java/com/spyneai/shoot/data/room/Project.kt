package com.spyneai.shoot.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Project(
    @PrimaryKey(autoGenerate = false)
    val uuid: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "project_name") val projectName: String,
    @ColumnInfo(name = "project_id") val projectId: String? = null,
    @ColumnInfo(name = "dynamic_layout") val dynamicLayout: String? = null,
    @ColumnInfo(name = "location_data") val locationData: String? = null,
    @ColumnInfo(name = "status") val status: String = "draft",
    @ColumnInfo(name = "rating") val rating: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
){
    @Ignore
    val skuCount: Int = 0
    @Ignore
    val imagesCount: Int = 0
    @Ignore
    val imagesList: List<String>? = null
}
