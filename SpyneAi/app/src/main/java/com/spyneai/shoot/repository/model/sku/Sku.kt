package com.spyneai.shoot.repository.model.sku

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Sku(
    @PrimaryKey(autoGenerate = false)
    val uuid: String,
    @ColumnInfo(name = "sku_name") val skuName: String,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "subcategory_name") val subcategoryName: String?,
    @ColumnInfo(name = "subcategory_id") val subcategoryId: String?,
    @ColumnInfo(name = "project_uuid") val projectUuid: String,
    @ColumnInfo(name = "project_id") val projectId: String? = null,
    @ColumnInfo(name = "sku_id") val skuId: String? = null,
    @ColumnInfo(name = "initial_frames") val initialFrames: Int? = null,
    @ColumnInfo(name = "is_360") val isThreeSixty: Boolean = false,
    @ColumnInfo(name = "total_frames") val totalFrames: Int?,
    @ColumnInfo(name = "background_name") val backgroundName: String? = null,
    @ColumnInfo(name = "background_id") val backgroundId: String? = null,
    @ColumnInfo(name = "additional_data") val additionalData: String? = null,
    @ColumnInfo(name = "is_processed") val isProcessed: Boolean = false,
    @ColumnInfo(name = "status") val status: String = "draft",
    @ColumnInfo(name = "is_paid") val isPaid: Boolean = false,
    @ColumnInfo(name = "rating") val rating: String? = null,
     @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
){
    @Ignore
    val imagesCount: Int? = null
    @Ignore
    val imagesList: List<String>? = null
}
