package com.spyneai.shoot.repository.model.image

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Image(
    @PrimaryKey(autoGenerate = false)
    var uuid: String = "",
    val enterprise_id: String? = null,
    val input_image_hres_url: String = "",
    val input_image_lres_url: String = "",
    val output_image_hres_url: String = "",
    val output_image_lres_url: String = "",
    val output_image_lres_wm_url: String = "",
    var isSelected : Boolean = false,
    var imageClicked : Boolean = false,
    var imagePath : String? = null,
    @SerializedName("project_uuid") val projectUuid: String? = null,
    @SerializedName("project_id") val projectId: String? = null,
    @SerializedName("sku_name") val skuName: String?,
    @SerializedName("sku_uuid") var skuUuid: String?,
    @SerializedName("sku_id") val skuId: String? = null,
    @SerializedName("image_name") val name: String,
    @SerializedName("image_category") val image_category: String,
    @SerializedName("frame_seq_no") val sequence: Int,
    @SerializedName("angle") val angle: Int,
    @SerializedName("overlay_id") var overlayId: String,
    @SerializedName("is_reclick") val isReclick: Boolean,
    @SerializedName("is_reshoot") val isReshoot: Boolean,
    @SerializedName("path") var path: String,
    @SerializedName("pre_signed_url") var preSignedUrl: String? = null,
    @SerializedName("image_id") var imageId: String? = null,
    @SerializedName("tags") var tags: String? = null,
    @SerializedName("debug_data") var debugData: String? = null,
    @SerializedName("to_process_at") val toProcessAT: Long = System.currentTimeMillis(),
    @SerializedName("retry_count") val retryCount: Int = 1,
    @SerializedName("is_uploaded") var isUploaded: Boolean = false,
    @SerializedName("is_marked_done") var isMarkedDone: Boolean = false,
    @SerializedName("status") val status: String = "draft",
    @SerializedName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)


