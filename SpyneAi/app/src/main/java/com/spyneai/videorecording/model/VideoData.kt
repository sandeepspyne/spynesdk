package com.spyneai.videorecording.model

data class VideoData(
    val n_frames: Int,
    val processed_image_list: List<String>,
    val type: String,
    val url: String
)