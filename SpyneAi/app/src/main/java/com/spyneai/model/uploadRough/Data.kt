package com.spyneai.model.uploadRough

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(
        val shootId: String,
        val skuId: String,
        val skuName: String,
        val photoNode: PhotoNode,
        val frame: Frame,
        val totalFrames: Int,
        val currentFrame: Int,
        val extraFrames: Int,
        val pendingFrame: Boolean
)
