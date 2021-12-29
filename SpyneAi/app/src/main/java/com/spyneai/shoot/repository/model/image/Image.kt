package com.spyneai.shoot.repository.model.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Image(
    @PrimaryKey(autoGenerate = false)
    val uuid: String = "",
    @ColumnInfo(name = "project_uuid") val projectUuid: String?,
    @ColumnInfo(name = "project_id") val projectId: String? = null,
    @ColumnInfo(name = "sku_name") val skuName: String?,
    @ColumnInfo(name = "sku_uuid") val skuUuid: String?,
    @ColumnInfo(name = "sku_id") val skuId: String? = null,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "sequence") val sequence: Int,
    @ColumnInfo(name = "angle") val angle: Int,
    @ColumnInfo(name = "overlay_id") val overlayId: String,
    @ColumnInfo(name = "is_reclick") val isReclick: Boolean,
    @ColumnInfo(name = "is_reshoot") val isReshoot: Boolean,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "pre_signed_url") val preSignedUrl: String? = null,
    @ColumnInfo(name = "image_id") val imageId: String? = null,
    @ColumnInfo(name = "tags") val tags: String? = null,
    @ColumnInfo(name = "debug_data") val debugData: String? = null,
    @ColumnInfo(name = "to_process_at") val toProcessAT: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    @ColumnInfo(name = "is_uploaded") val isUploaded: Boolean = false,
    @ColumnInfo(name = "is_marked_done") val isMarkedDone: Boolean = false,
    @ColumnInfo(name = "status") val status: String = "draft",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
