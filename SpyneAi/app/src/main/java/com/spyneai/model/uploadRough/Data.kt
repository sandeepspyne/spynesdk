package com.spyneai.model.uploadRough

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(
        @SerializedName("shootId")
        val shootId: String,
        @SerializedName("skuId")
        val skuId: String,
        @SerializedName("skuName")
        val skuName: String,
        @SerializedName("photoNode")
        val photoNode: PhotoNode,
        @SerializedName("frame")
        val frame: Frame,
        @SerializedName("totalFrames")
        val totalFrames: Int,
        @SerializedName("currentFrame")
        val currentFrame: Int,
        @SerializedName("extraFrames")
        val extraFrames: Int,
        @SerializedName("pendingFrame")
        val pendingFrame: Boolean
)
