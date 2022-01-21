package com.spyneai.shoot.repository.model.sku

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.spyneai.needs.AppConstants
import org.json.JSONObject
import java.util.*

@Entity
class Sku(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("local_id")
    var uuid: String,
    val isCreated: Boolean = false,
    var isSelectAble: Boolean = false,
    val toProcessAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 1,
    @SerializedName("sku_name")
    var skuName: String?= null,
    @SerializedName("category") var categoryName: String? = null,
    @SerializedName("category_id") var categoryId: String? = null,
    @SerializedName("sub_category") var subcategoryName: String? = null,
    @SerializedName("sub_category_id") var subcategoryId: String? = null,
    @SerializedName("project_uuid") var projectUuid: String?= null,
    @SerializedName("project_id") var projectId: String? = null,
    @SerializedName("sku_id") var skuId: String? = null,
    @SerializedName("exterior_click") var initialFrames: Int? = null,
    @SerializedName("is_360") var isThreeSixty: Boolean = false,
    @SerializedName("total_frames_no") var totalFrames: Int? = 0,
    @SerializedName("360_frames") var threeSixtyFrames: Int? = 0,
    @SerializedName("background_name") var backgroundName: String? = null,
    @SerializedName("background_id") var backgroundId: String = AppConstants.DEFAULT_BG_ID,
    @SerializedName("additional_data") var additionalData: String? = null,
    @SerializedName("is_processed") var isProcessed: Boolean = false,
    @SerializedName("status") var status: String = "draft",
    @SerializedName("is_paid") var isPaid: Boolean = false,
    @SerializedName("rating") var rating: String? = null,
    @SerializedName("display_image") var thumbnail: String? = null,
    @SerializedName("video_id") val videoId : String? = null,
    @SerializedName("video_url") val videoUrl : String? = null,
    @SerializedName("created_on") var createdOn: String = Date(System.currentTimeMillis()).toString(),
    @SerializedName("created_at") var createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at") var updatedAt: Long = System.currentTimeMillis(),
    @SerializedName("total_images_captured")
    var imagesCount: Int = 0,
    @SerializedName("processed_images")
    var processedImages: Int = 0,
    val imagePresent: Int = 1,
    val videoPresent: Int = 0,
    var totalFramesUpdated: Boolean = false
)
