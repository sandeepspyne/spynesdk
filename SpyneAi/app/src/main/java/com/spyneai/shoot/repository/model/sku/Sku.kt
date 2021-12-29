package com.spyneai.shoot.repository.model.sku

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Sku(
    @PrimaryKey(autoGenerate = false)
    var uuid: String?,
    @ColumnInfo(name = "sku_name") var skuName: String?,
    @ColumnInfo(name = "category_name") var categoryName: String?,
    @ColumnInfo(name = "category_id") var categoryId: String?,
    @ColumnInfo(name = "subcategory_name") var subcategoryName: String?,
    @ColumnInfo(name = "subcategory_id") var subcategoryId: String?,
    @ColumnInfo(name = "project_uuid") var projectUuid: String,
    @ColumnInfo(name = "project_id") var projectId: String? = null,
    @ColumnInfo(name = "sku_id") var skuId: String? = null,
    @ColumnInfo(name = "initial_frames") var initialFrames: Int? = null,
    @ColumnInfo(name = "is_360") var isThreeSixty: Boolean = false,
    @ColumnInfo(name = "total_frames") var totalFrames: Int?,
    @ColumnInfo(name = "background_name") var backgroundName: String? = null,
    @ColumnInfo(name = "background_id") var backgroundId: String? = null,
    @ColumnInfo(name = "additional_data") var additionalData: String? = null,
    @ColumnInfo(name = "is_processed") var isProcessed: Boolean = false,
    @ColumnInfo(name = "status") var status: String = "draft",
    @ColumnInfo(name = "is_paid") var isPaid: Boolean = false,
    @ColumnInfo(name = "rating") var rating: String? = null,
     @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") var updatedAt: Long = System.currentTimeMillis()
){
    @Ignore
    var imagesCount: Int? = null
    @Ignore
    var imagesList: List<String>? = null
}
