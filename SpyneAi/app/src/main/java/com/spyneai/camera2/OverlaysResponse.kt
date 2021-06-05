package com.spyneai.camera2

data class OverlaysResponse(
    val `data`: List<Data>,
    val message: String,
    val status: Int
) {
    data class Data(
        val active: Int,
        val angle_name: String,
        val angles: Int,
        val created_at: String,
        val display_name: String,
        val display_thumbnail: String,
        val enterprise_id: String,
        val frame_angle: String,
        val id: Int,
        val overlay_id: String,
        val priority: Int,
        val prod_cat_id: String,
        val prod_sub_cat_id: String,
        val type: String,
        val updated_at: String
    )
}