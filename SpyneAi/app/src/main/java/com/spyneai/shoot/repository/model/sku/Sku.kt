package com.spyneai.shoot.repository.model.sku

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.spyneai.needs.AppConstants
import org.json.JSONObject

@Entity
class Sku(
    @PrimaryKey(autoGenerate = false)
    var uuid: String,
    val isCreated: Boolean = false,
    var isSelectAble: Boolean = false,
    val toProcessAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 1,
    @SerializedName("sku_name")
    @ColumnInfo(name = "sku_name")
    var skuName: String?= null,
    @ColumnInfo(name = "category_name") var categoryName: String? = null,
    @ColumnInfo(name = "category_id") var categoryId: String? = null,
    @ColumnInfo(name = "subcategory_name") var subcategoryName: String? = null,
    @ColumnInfo(name = "subcategory_id") var subcategoryId: String? = null,
    @ColumnInfo(name = "project_uuid") var projectUuid: String?= null,
    @ColumnInfo(name = "project_id") var projectId: String? = null,
    @ColumnInfo(name = "sku_id") var skuId: String? = null,
    @ColumnInfo(name = "initial_frames") var initialFrames: Int? = null,
    @ColumnInfo(name = "is_360") var isThreeSixty: Boolean = false,
    @ColumnInfo(name = "total_frames") var totalFrames: Int? = 0,
    @ColumnInfo(name = "360_frames") var threeSixtyFrames: Int? = 0,
    @ColumnInfo(name = "background_name") var backgroundName: String? = null,
    @ColumnInfo(name = "background_id") var backgroundId: String = AppConstants.DEFAULT_BG_ID,
    @ColumnInfo(name = "additional_data") var additionalData: String? = null,
    @ColumnInfo(name = "is_processed") var isProcessed: Boolean = false,
    @ColumnInfo(name = "status") var status: String = "draft",
    @ColumnInfo(name = "is_paid") var isPaid: Boolean = false,
    @ColumnInfo(name = "rating") var rating: String? = null,
    @SerializedName("thumbnail") var thumbnail: String? = null,
    @SerializedName("video_id") val videoId : String? = null,
    @SerializedName("video_url") val videoUrl : String? = null,
    @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") var updatedAt: Long = System.currentTimeMillis(),
    var imagesCount: Int = 0,
    val imagePresent: Int = 1,
    val videoPresent: Int = 0,
    var totalFramesUpdated: Boolean = false
)
