package com.spyneai.shoot.data.model

import com.google.gson.annotations.SerializedName

data class ShootData(
    @SerializedName("capturedImage") var capturedImage: String,
    @SerializedName("project_id") val project_id: String,
    @SerializedName("sku_id") val sku_id: String,
    @SerializedName("image_category") val image_category: String,
    @SerializedName("auth_key") val auth_key: String,
    val overlayId : Int = 0,
    val sequence: Int = 0,
    var angle: Int = 0,
    var name : String = "",
    var debugData : String = "",
    var meta: String = "",
    var isSelected : Boolean = false,
    var imageClicked : Boolean = false,
    var imagePath : String = ""
    )
