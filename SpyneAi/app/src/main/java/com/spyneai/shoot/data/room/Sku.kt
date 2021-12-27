package com.spyneai.shoot.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sku(
    @PrimaryKey(autoGenerate = false)
    val uuid: String = "",
    @ColumnInfo(name = "sku_name") val skuName: String?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "subcategory_name") val subcategoryName: String?,
    @ColumnInfo(name = "subcategory_id") val subcategoryId: String?,
    @ColumnInfo(name = "project_uuid") val projectUuid: String?,
    @ColumnInfo(name = "project_id") val projectId: String?,
    @ColumnInfo(name = "sku_id") val skuId: String?,
    @ColumnInfo(name = "initial_frames") val initialFrames: Int?,
    @ColumnInfo(name = "is_360") val isThreeSixty: Boolean?,
    @ColumnInfo(name = "total_frames") val totalFrames: Int?,
    @ColumnInfo(name = "background_name") val backgroundName: String?,
    @ColumnInfo(name = "background_id") val backgroundId: String?,
    @ColumnInfo(name = "additional_data") val additionalData: String?,
    @ColumnInfo(name = "is_processed") val isProcessed: Boolean?,
    @ColumnInfo(name = "status") val status: String?,
    @ColumnInfo(name = "is_paid") val isPaid: Boolean?,
    @ColumnInfo(name = "rating") val rating: String?,
     @ColumnInfo(name = "created_at") val createdAt: Long?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long?,
    val imagesCount: String?,
    val imagesList: List<String>?
)
