package com.spyneai.threesixty.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity
data class VideoDetails(
    @PrimaryKey(autoGenerate = false)
    val uuid: String = "",
    var projectUuid: String,
    var skuUuid: String,
    var projectId: String? = null,
    var skuName: String? = null,
    var skuId: String? = null,
    var categoryName: String = "Automobiles",
    var categoryId: String? = null,
    var subCategory: String = "sedan",
    var type: String = "360_exterior",
    var videoPath: String? = null,
    var shootMode: Int = 0,
    var frames: Int = 0,
    var backgroundId: String? = null,
    var sample360: String? = null,
    var isUploaded: Boolean = false,
    var isMarkedDone: Boolean = false,
    var preSignedUrl: String? = null,
    var videoId: String? = null,
    val toProcessAT: Long = System.currentTimeMillis(),
    val retryCount: Int = 1
)