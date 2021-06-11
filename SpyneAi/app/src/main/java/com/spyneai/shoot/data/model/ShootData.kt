package com.spyneai.shoot.data.model

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class ShootData(
    @SerializedName("uri") val uri: Uri,
    @SerializedName("project_id") val project_id: String,
    @SerializedName("sku_id") val sku_id: String,
    @SerializedName("image_category") val image_category: String,
    @SerializedName("auth_key") val auth_key: String
    )
