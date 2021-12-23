package com.spyneai.shoot.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Image(
    @PrimaryKey(autoGenerate = false)
    val uuid: String = "",
    @ColumnInfo(name = "project_uuid") val projectUuid: String?,
    @ColumnInfo(name = "project_id") val projectId: String?,
    @ColumnInfo(name = "sku_name") val skuName: String?,
    @ColumnInfo(name = "sku_uuid") val skuUuid: String?,
    @ColumnInfo(name = "sku_id") val skuId: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "type") val type: String?,
    @ColumnInfo(name = "sequence") val sequence: String?,
    @ColumnInfo(name = "angle") val angle: String?,
    @ColumnInfo(name = "overlay_id") val overlayId: String?,
    @ColumnInfo(name = "is_reclick") val isReclick: String?,
    @ColumnInfo(name = "is_reshoot") val isReshoot: String?,
    @ColumnInfo(name = "path") val path: String?,
    @ColumnInfo(name = "pre_signed_url") val preSignedUrl: String?,
    @ColumnInfo(name = "image_id") val imageId: String?,
    @ColumnInfo(name = "tags") val tags: String?,
    @ColumnInfo(name = "debug_data") val debugData: String?,
    @ColumnInfo(name = "to_process_at") val toProcessAT: Long?,
    @ColumnInfo(name = "retry_count") val retryCount: String?,
    @ColumnInfo(name = "is_uploaded") val isUploaded: Boolean?,
    @ColumnInfo(name = "is_marked_done") val isMarkedDone: Boolean?,
    @ColumnInfo(name = "status") val status: String?,
    @ColumnInfo(name = "created_on") val createdOn: String?,
    @ColumnInfo(name = "updated_on") val updatedOn: String?
)
