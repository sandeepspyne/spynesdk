package com.spyneai.videorecording.model

data class VideoProcessingResponse(
    val category: String,
    val id: String,
    val process_status: Boolean,
    val sku_id: String,
    val sku_name: String,
    val subcategory: String,
    val user_id: String,
    val video_data: List<VideoData>
)