package com.spyneai.camera2

import androidx.room.Entity
import androidx.room.PrimaryKey

data class OverlaysResponse(
    val `data`: List<Overlays>,
    val message: String,
    val status: Int
) {
    @Entity
    data class Overlays(
        @PrimaryKey(autoGenerate = true)
        val uuid : Int,
        val overlay_id: String,
        var fetchAngle : Int = 0,
        val active: Int,
        val angle_name: String,
        val angles: Int,
        val created_at: String,
        val display_name: String,
        val display_thumbnail: String,
        val enterprise_id: String,
        val frame_angle: String,
        val id: Int,
        val priority: Int,
        val prod_cat_id: String,
        val prod_sub_cat_id: String,
        val type: String,
        val updated_at: String,
        var isSelected : Boolean = false,
        var imageClicked : Boolean = false,
        var imagePath : String?,
        var sequenceNumber : Int = 0
    )
}