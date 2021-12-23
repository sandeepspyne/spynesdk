package com.spyneai.shoot.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Project(
    @PrimaryKey(autoGenerate = false)
    val uuid: String = "",
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "project_name") val projectName: String?,
    @ColumnInfo(name = "project_id") val projectId: String?,
    @ColumnInfo(name = "dynamic_layout") val dynamicLayout: String?,
    @ColumnInfo(name = "location_data") val locationData: String?,
    @ColumnInfo(name = "status") val status: String?,
    @ColumnInfo(name = "rating") val rating: String?,
    @ColumnInfo(name = "created_on") val createdOn: String?,
    @ColumnInfo(name = "updated_on") val updatedOn: String?,
    val skuCount: String?,
    val imagesCount: String?,
    val imagesList: String?
)
